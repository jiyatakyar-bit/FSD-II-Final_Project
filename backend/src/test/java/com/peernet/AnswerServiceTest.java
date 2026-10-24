package com.peernet;

import com.peernet.config.ApiException;
import com.peernet.config.RateLimiter;
import com.peernet.model.Doubt;
import com.peernet.model.DoubtStatus;
import com.peernet.model.Role;
import com.peernet.repository.AnswerRepository;
import com.peernet.repository.AnswerVoteRepository;
import com.peernet.repository.DoubtRepository;
import com.peernet.repository.HelperExpertiseRepository;
import com.peernet.repository.ReportRepository;
import com.peernet.repository.UserRepository;
import com.peernet.security.CurrentUser;
import com.peernet.service.AnswerService;
import com.peernet.service.DtoMapper;
import com.peernet.service.NotificationService;
import com.peernet.service.ReputationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnswerServiceTest {
    @Mock AnswerRepository answerRepository;
    @Mock AnswerVoteRepository voteRepository;
    @Mock DoubtRepository doubtRepository;
    @Mock UserRepository userRepository;
    @Mock HelperExpertiseRepository expertiseRepository;
    @Mock ReportRepository reportRepository;
    @Mock ReputationService reputationService;
    @Mock NotificationService notificationService;
    @Mock DtoMapper mapper;
    @Mock RateLimiter rateLimiter;
    @InjectMocks AnswerService answerService;

    @Test
    void cannotAnswerResolvedDoubt() {
        assertCannotAnswer(DoubtStatus.RESOLVED, "This doubt is already resolved");
    }

    @Test
    void cannotAnswerClosedDoubt() {
        assertCannotAnswer(DoubtStatus.CLOSED, "This doubt is closed");
    }

    private void assertCannotAnswer(DoubtStatus status, String expectedMessage) {
        Doubt doubt = new Doubt();
        doubt.setStatus(status);
        when(doubtRepository.findById(1L)).thenReturn(Optional.of(doubt));

        ApiException error = assertThrows(ApiException.class,
                () -> answerService.post(new CurrentUser(2L, "helper@peernet.com", Role.STUDENT), 1L, "Answer"));

        assertEquals(409, error.getStatus());
        assertEquals(expectedMessage, error.getMessage());
        verify(userRepository, never()).findById(anyLong());
        verify(answerRepository, never()).save(any());
    }
}
