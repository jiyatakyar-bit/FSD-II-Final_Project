package com.peernet.service;

import com.peernet.dto.Dtos.MatchDto;
import com.peernet.model.AppUser;
import com.peernet.model.Doubt;
import com.peernet.model.DoubtAssignment;
import com.peernet.model.DoubtStatus;
import com.peernet.model.HelperExpertise;
import com.peernet.model.Role;
import com.peernet.repository.DoubtAssignmentRepository;
import com.peernet.repository.HelperExpertiseRepository;
import com.peernet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Smart matching:
 * Score = wT*TopicMatch + wR*Reputation + wA*Availability + wP*Responsiveness
 * (weights are editable by the admin).
 */
@Service
@RequiredArgsConstructor
public class MatchingService {
    private final UserRepository userRepository;
    private final HelperExpertiseRepository expertiseRepository;
    private final DoubtAssignmentRepository assignmentRepository;
    private final SettingsService settings;
    private final NotificationService notificationService;

    public static double round(double v) {
        return Math.round(v * 1000.0) / 1000.0;
    }

    /** Ranks all eligible helpers for the doubt, best first. */
    public List<MatchDto> rank(Doubt doubt, boolean includeMentors) {
        List<Role> roles = includeMentors ? List.of(Role.STUDENT, Role.MENTOR) : List.of(Role.STUDENT);
        List<AppUser> candidates = userRepository.findByRoleInAndActiveTrue(roles).stream()
                .filter(u -> !u.getId().equals(doubt.getAsker().getId())).toList();
        int maxRep = 1;
        for (AppUser u : candidates) {
            maxRep = Math.max(maxRep, u.getReputation());
        }
        double wTopic = settings.getDouble("w_topic");
        double wRep = settings.getDouble("w_reputation");
        double wAvail = settings.getDouble("w_availability");
        double wResp = settings.getDouble("w_responsiveness");

        List<MatchDto> result = new ArrayList<>();
        for (AppUser u : candidates) {
            double topic = topicScore(u.getId(), doubt.getSubject().getId());
            double reputation = u.getReputation() / (double) maxRep;
            long open = assignmentRepository.countByHelperIdAndDoubtStatusIn(u.getId(), List.of(DoubtStatus.ASSIGNED));
            double availability = 1.0 / (1 + open);
            double responsiveness = u.getAnswersCount() == 0 ? 0.5
                    : 1.0 / (1 + (u.getTotalResponseMinutes() / (double) u.getAnswersCount()) / 60.0);
            double score = wTopic * topic + wRep * reputation + wAvail * availability + wResp * responsiveness;
            result.add(new MatchDto(u.getId(), u.getName(), u.getRole().name(), round(topic), round(reputation),
                    round(availability), round(responsiveness), round(score)));
        }
        result.sort((a, b) -> Double.compare(b.score(), a.score()));
        return result;
    }

    /** 0.5 if the helper declared the subject + up to 0.5 for accepted answers in it (5 accepted answers = full). */
    double topicScore(Long userId, Long subjectId) {
        HelperExpertise e = expertiseRepository.findByUserIdAndSubjectId(userId, subjectId).orElse(null);
        if (e == null) return 0;
        return (e.isDeclared() ? 0.5 : 0) + Math.min(0.5, e.getProvenCount() / 5.0);
    }

    /** Sends the doubt to the next best helpers (more helpers for urgent doubts, mentors after 2 escalations). */
    @Transactional
    public List<AppUser> assign(Doubt doubt) {
        boolean mentors = doubt.getEscalationLevel() >= 2;
        Set<Long> already = assignmentRepository.findByDoubtId(doubt.getId()).stream()
                .map(a -> a.getHelper().getId()).collect(Collectors.toSet());
        List<MatchDto> ranked = rank(doubt, mentors).stream().filter(m -> !already.contains(m.userId())).toList();
        if (ranked.isEmpty() && !mentors) {
            ranked = rank(doubt, true).stream().filter(m -> !already.contains(m.userId())).toList();
        }
        int count = (int) settings.getDouble("helpers_per_round") + (doubt.isUrgent() ? 2 : 0);

        List<AppUser> chosen = new ArrayList<>();
        for (MatchDto m : ranked.stream().limit(count).toList()) {
            AppUser helper = userRepository.findById(m.userId()).orElseThrow();
            DoubtAssignment a = new DoubtAssignment();
            a.setDoubt(doubt);
            a.setHelper(helper);
            a.setScore(m.score());
            a.setRoundNo(doubt.getEscalationLevel());
            assignmentRepository.save(a);
            notificationService.notify(helper, "ASSIGNED",
                    "A doubt in " + doubt.getSubject().getName() + " matches your skills: " + doubt.getTitle(),
                    doubt.getId());
            chosen.add(helper);
        }
        doubt.setLastAssignedAt(LocalDateTime.now());
        if (!chosen.isEmpty() && doubt.getStatus() == DoubtStatus.OPEN) {
            doubt.setStatus(DoubtStatus.ASSIGNED);
        }
        return chosen;
    }
}
