package com.peernet.service;

import com.peernet.config.ApiException;
import com.peernet.dto.Dtos.AdminUserDto;
import com.peernet.dto.Dtos.MatchDto;
import com.peernet.dto.Dtos.ReportDto;
import com.peernet.dto.Dtos.StatsDto;
import com.peernet.model.AppUser;
import com.peernet.model.Doubt;
import com.peernet.model.DoubtStatus;
import com.peernet.model.Report;
import com.peernet.model.Role;
import com.peernet.repository.AnswerRepository;
import com.peernet.repository.DoubtRepository;
import com.peernet.repository.ReportRepository;
import com.peernet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final DoubtRepository doubtRepository;
    private final AnswerRepository answerRepository;
    private final ReportRepository reportRepository;
    private final LeaderboardService leaderboardService;
    private final MatchingService matchingService;
    private final ReputationService reputationService;

    @Transactional(readOnly = true)
    public StatsDto stats() {
        long total = doubtRepository.count();
        long resolved = doubtRepository.countByStatus(DoubtStatus.RESOLVED);
        long open = doubtRepository.countByStatusIn(List.of(DoubtStatus.OPEN, DoubtStatus.ASSIGNED));
        double avgMinutes = doubtRepository.findByFirstAnsweredAtNotNull().stream()
                .mapToLong(d -> Duration.between(d.getCreatedAt(), d.getFirstAnsweredAt()).toMinutes())
                .average().orElse(0);
        Map<String, Long> bySubject = new LinkedHashMap<>();
        for (Object[] row : doubtRepository.countBySubject()) {
            bySubject.put((String) row[0], ((Number) row[1]).longValue());
        }
        double rate = total == 0 ? 0 : Math.round(resolved * 1000.0 / total) / 10.0;
        return new StatsDto(userRepository.count(), total, open, resolved, answerRepository.count(), rate,
                Math.round(avgMinutes * 10.0) / 10.0, bySubject, leaderboardService.overall());
    }

    public List<AdminUserDto> users() {
        return userRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(u -> new AdminUserDto(u.getId(), u.getName(), u.getEmail(), u.getRole().name(),
                        u.getReputation(), u.isActive())).toList();
    }

    @Transactional
    public void setRole(Long id, String role) {
        AppUser u = userRepository.findById(id).orElseThrow(() -> new ApiException(404, "User not found"));
        try {
            u.setRole(Role.valueOf(role.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ApiException(400, "Role must be STUDENT, MENTOR or ADMIN");
        }
        userRepository.save(u);
    }

    @Transactional
    public void setActive(Long id, boolean active) {
        AppUser u = userRepository.findById(id).orElseThrow(() -> new ApiException(404, "User not found"));
        u.setActive(active);
        userRepository.save(u);
    }

    @Transactional(readOnly = true)
    public List<ReportDto> reports() {
        return reportRepository.findByResolvedFalseOrderByCreatedAtDesc().stream()
                .map(r -> new ReportDto(r.getId(), r.getAnswer().getId(), r.getAnswer().getDoubt().getId(),
                        r.getAnswer().getContent(), r.getReporter().getName(), r.getReason(), r.getCreatedAt()))
                .toList();
    }

    /** uphold = true takes 3 reputation points from the author of the reported answer. */
    @Transactional
    public void resolveReport(Long id, boolean uphold) {
        Report r = reportRepository.findById(id).orElseThrow(() -> new ApiException(404, "Report not found"));
        if (uphold) {
            reputationService.award(r.getAnswer().getHelper(), "REPORT_UPHELD", -3);
        }
        r.setResolved(true);
        reportRepository.save(r);
    }

    /** Shows the score breakdown of the matching algorithm for a doubt (useful for demos and viva). */
    @Transactional(readOnly = true)
    public List<MatchDto> matching(Long doubtId) {
        Doubt d = doubtRepository.findById(doubtId).orElseThrow(() -> new ApiException(404, "Doubt not found"));
        return matchingService.rank(d, true).stream().limit(10).toList();
    }
}
