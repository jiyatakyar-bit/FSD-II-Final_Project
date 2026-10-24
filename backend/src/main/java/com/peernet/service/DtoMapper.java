package com.peernet.service;

import com.peernet.dto.Dtos.AnswerDto;
import com.peernet.dto.Dtos.DoubtDto;
import com.peernet.dto.Dtos.UserDto;
import com.peernet.model.Answer;
import com.peernet.model.AnswerVote;
import com.peernet.model.AppUser;
import com.peernet.model.Badge;
import com.peernet.model.Doubt;
import com.peernet.model.HelperExpertise;
import com.peernet.model.Role;
import com.peernet.repository.AnswerRepository;
import com.peernet.repository.AnswerVoteRepository;
import com.peernet.repository.BadgeRepository;
import com.peernet.repository.HelperExpertiseRepository;
import com.peernet.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DtoMapper {
    private final BadgeRepository badgeRepository;
    private final HelperExpertiseRepository expertiseRepository;
    private final AnswerRepository answerRepository;
    private final AnswerVoteRepository voteRepository;

    public UserDto user(AppUser u) {
        List<String> badges = badgeRepository.findByUserId(u.getId()).stream().map(Badge::getName).toList();
        List<Long> subjects = expertiseRepository.findByUserId(u.getId()).stream()
                .filter(HelperExpertise::isDeclared).map(e -> e.getSubject().getId()).toList();
        return new UserDto(u.getId(), u.getName(), u.getEmail(), u.getRole().name(), u.getReputation(),
                u.level(), badges, subjects);
    }

    public AnswerDto answer(Answer a, Long viewerId) {
        int myVote = viewerId == null ? 0
                : voteRepository.findByAnswerIdAndUserId(a.getId(), viewerId).map(AnswerVote::getValue).orElse(0);
        AppUser h = a.getHelper();
        return new AnswerDto(a.getId(), a.getContent(), h.getId(), h.getName(), h.level(), h.getReputation(),
                a.getVotes(), a.isAccepted(), a.isVerified(), a.getCreatedAt(), myVote);
    }

    /** Builds the doubt response. The asker's identity is hidden for anonymous doubts (except for the asker and admins). */
    public DoubtDto doubt(Doubt d, CurrentUser viewer, boolean withAnswers) {
        boolean mine = viewer != null && d.getAsker().getId().equals(viewer.id());
        boolean admin = viewer != null && viewer.role() == Role.ADMIN;
        boolean reveal = !d.isAnonymous() || mine || admin;
        String askerName = reveal ? d.getAsker().getName() : "Anonymous Student";
        if (d.isAnonymous() && admin && !mine) {
            askerName = askerName + " (posted anonymously)";
        }
        Long askerId = reveal ? d.getAsker().getId() : null;
        List<AnswerDto> answers = null;
        if (withAnswers) {
            Long viewerId = viewer == null ? null : viewer.id();
            answers = answerRepository.findByDoubtIdOrderByAcceptedDescVotesDescCreatedAtAsc(d.getId())
                    .stream().map(a -> answer(a, viewerId)).toList();
        }
        return new DoubtDto(d.getId(), d.getTitle(), d.getDescription(), d.getTags(), d.getCodeSnippet(),
                d.getSubject().getId(), d.getSubject().getName(), askerId, askerName, mine, d.isAnonymous(),
                d.isUrgent(), d.getStatus().name(), d.getEscalationLevel(),
                answerRepository.countByDoubtId(d.getId()), d.getCreatedAt(), answers);
    }
}
