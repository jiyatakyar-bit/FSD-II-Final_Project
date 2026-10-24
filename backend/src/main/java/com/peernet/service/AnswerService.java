package com.peernet.service;

import com.peernet.config.ApiException;
import com.peernet.config.RateLimiter;
import com.peernet.dto.Dtos.AnswerDto;
import com.peernet.model.Answer;
import com.peernet.model.AnswerVote;
import com.peernet.model.AppUser;
import com.peernet.model.Doubt;
import com.peernet.model.DoubtStatus;
import com.peernet.model.HelperExpertise;
import com.peernet.model.Report;
import com.peernet.repository.AnswerRepository;
import com.peernet.repository.AnswerVoteRepository;
import com.peernet.repository.DoubtRepository;
import com.peernet.repository.HelperExpertiseRepository;
import com.peernet.repository.ReportRepository;
import com.peernet.repository.UserRepository;
import com.peernet.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Reputation rules: answer posted +5, upvote +2, downvote -3, accepted answer +15,
 * mentor-verified answer +10, answer removed after an upheld report -3.
 */
@Service
@RequiredArgsConstructor
public class AnswerService {
    private final AnswerRepository answerRepository;
    private final AnswerVoteRepository voteRepository;
    private final DoubtRepository doubtRepository;
    private final UserRepository userRepository;
    private final HelperExpertiseRepository expertiseRepository;
    private final ReportRepository reportRepository;
    private final ReputationService reputationService;
    private final NotificationService notificationService;
    private final DtoMapper mapper;
    private final RateLimiter rateLimiter;

    @Transactional
    public AnswerDto post(CurrentUser me, Long doubtId, String content) {
        rateLimiter.check(me.id(), "answer", 10, 60_000);
        Doubt d = doubtRepository.findById(doubtId).orElseThrow(() -> new ApiException(404, "Doubt not found"));
        if (d.getStatus() == DoubtStatus.CLOSED) {
            throw new ApiException(409, "This doubt is closed");
        }
        if (d.getStatus() == DoubtStatus.RESOLVED) {
            throw new ApiException(409, "This doubt is already resolved");
        }
        if (d.getAsker().getId().equals(me.id())) {
            throw new ApiException(400, "You cannot answer your own doubt");
        }
        AppUser helper = userRepository.findById(me.id()).orElseThrow(() -> new ApiException(404, "User not found"));

        Answer a = new Answer();
        a.setDoubt(d);
        a.setHelper(helper);
        a.setContent(content.trim());
        a = answerRepository.save(a);

        long minutes = Duration.between(d.getCreatedAt(), LocalDateTime.now()).toMinutes();
        helper.setAnswersCount(helper.getAnswersCount() + 1);
        helper.setTotalResponseMinutes(helper.getTotalResponseMinutes() + minutes);
        if (d.getFirstAnsweredAt() == null) {
            d.setFirstAnsweredAt(LocalDateTime.now());
        }
        if (d.getStatus() == DoubtStatus.OPEN || d.getStatus() == DoubtStatus.ASSIGNED) {
            d.setStatus(DoubtStatus.ANSWERED);
        }
        doubtRepository.save(d);

        reputationService.award(helper, "ANSWER_POSTED", 5);
        if (answerRepository.countByHelperId(helper.getId()) == 1) {
            reputationService.grantBadge(helper, "First Answer");
        }
        notificationService.notify(d.getAsker(), "NEW_ANSWER",
                "Your doubt \"" + d.getTitle() + "\" received a new answer.", d.getId());
        return mapper.answer(a, me.id());
    }

    private static int points(int vote) {
        return vote == 1 ? 2 : (vote == -1 ? -3 : 0);
    }

    /** value = 1 (upvote) or -1 (downvote). Sending the same value again removes the vote. */
    @Transactional
    public AnswerDto vote(CurrentUser me, Long answerId, int value) {
        if (value != 1 && value != -1) {
            throw new ApiException(400, "Vote must be 1 or -1");
        }
        Answer a = find(answerId);
        if (a.getHelper().getId().equals(me.id())) {
            throw new ApiException(400, "You cannot vote on your own answer");
        }
        AppUser voter = userRepository.findById(me.id()).orElseThrow();
        AnswerVote v = voteRepository.findByAnswerIdAndUserId(answerId, me.id()).orElse(null);
        int previous = v == null ? 0 : v.getValue();
        int next = previous == value ? 0 : value;

        a.setVotes(a.getVotes() + next - previous);
        answerRepository.save(a);
        if (next == 0) {
            voteRepository.delete(v);
        } else {
            if (v == null) {
                v = new AnswerVote();
                v.setAnswer(a);
                v.setUser(voter);
            }
            v.setValue(next);
            voteRepository.save(v);
        }
        reputationService.award(a.getHelper(), "VOTE", points(next) - points(previous));
        return mapper.answer(a, me.id());
    }

    /** Only the person who asked can accept an answer; this resolves the doubt. */
    @Transactional
    public AnswerDto accept(CurrentUser me, Long answerId) {
        Answer a = find(answerId);
        Doubt d = a.getDoubt();
        if (!d.getAsker().getId().equals(me.id())) {
            throw new ApiException(403, "Only the person who asked can accept an answer");
        }
        if (d.getStatus() == DoubtStatus.RESOLVED) {
            throw new ApiException(409, "This doubt is already resolved");
        }
        a.setAccepted(true);
        answerRepository.save(a);
        d.setStatus(DoubtStatus.RESOLVED);
        d.setResolvedAt(LocalDateTime.now());
        doubtRepository.save(d);

        AppUser helper = a.getHelper();
        reputationService.award(helper, "ANSWER_ACCEPTED", 15);
        reputationService.grantBadge(helper, "Problem Solver");

        HelperExpertise e = expertiseRepository.findByUserIdAndSubjectId(helper.getId(), d.getSubject().getId())
                .orElseGet(() -> {
                    HelperExpertise created = new HelperExpertise();
                    created.setUser(helper);
                    created.setSubject(d.getSubject());
                    return created;
                });
        e.setProvenCount(e.getProvenCount() + 1);
        expertiseRepository.save(e);

        notificationService.notify(helper, "ACCEPTED",
                "Your answer to \"" + d.getTitle() + "\" was accepted. +15 reputation!", d.getId());
        return mapper.answer(a, me.id());
    }

    /** Mentors / admins can verify a correct answer. */
    @Transactional
    public AnswerDto verify(CurrentUser me, Long answerId) {
        Answer a = find(answerId);
        if (a.isVerified()) {
            throw new ApiException(409, "Already verified");
        }
        a.setVerified(true);
        answerRepository.save(a);
        reputationService.award(a.getHelper(), "ANSWER_VERIFIED", 10);
        notificationService.notify(a.getHelper(), "VERIFIED",
                "A mentor verified your answer to \"" + a.getDoubt().getTitle() + "\". +10 reputation!",
                a.getDoubt().getId());
        return mapper.answer(a, me.id());
    }

    @Transactional
    public void report(CurrentUser me, Long answerId, String reason) {
        Answer a = find(answerId);
        AppUser reporter = userRepository.findById(me.id()).orElseThrow();
        Report r = new Report();
        r.setAnswer(a);
        r.setReporter(reporter);
        r.setReason(reason.trim());
        reportRepository.save(r);
    }

    private Answer find(Long id) {
        return answerRepository.findById(id).orElseThrow(() -> new ApiException(404, "Answer not found"));
    }
}
