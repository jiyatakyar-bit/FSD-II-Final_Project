package com.peernet;

import com.peernet.model.AppUser;
import com.peernet.repository.BadgeRepository;
import com.peernet.repository.ReputationLogRepository;
import com.peernet.repository.UserRepository;
import com.peernet.service.NotificationService;
import com.peernet.service.ReputationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReputationServiceTest {
    @Mock UserRepository userRepository;
    @Mock ReputationLogRepository logRepository;
    @Mock BadgeRepository badgeRepository;
    @Mock NotificationService notificationService;
    @InjectMocks ReputationService reputationService;

    AppUser user;

    @BeforeEach
    void setUp() {
        user = new AppUser();
        user.setId(1L);
        user.setName("Test");
        user.setReputation(10);
    }

    @Test
    void awardAddsPointsAndLogsEvent() {
        reputationService.award(user, "ANSWER_POSTED", 5);
        assertEquals(15, user.getReputation());
        verify(logRepository).save(any());
    }

    @Test
    void reputationNeverGoesBelowZero() {
        reputationService.award(user, "VOTE", -50);
        assertEquals(0, user.getReputation());
    }

    @Test
    void risingStarBadgeIsGrantedAt100Points() {
        user.setReputation(95);
        when(badgeRepository.existsByUserIdAndName(1L, "Rising Star")).thenReturn(false);
        reputationService.award(user, "ANSWER_ACCEPTED", 15);
        verify(badgeRepository).save(any());
        verify(notificationService).notify(any(), anyString(), anyString(), any());
    }

    @Test
    void noBadgeBelow100Points() {
        reputationService.award(user, "ANSWER_POSTED", 5);
        verify(badgeRepository, never()).save(any());
    }

    @Test
    void levelsFollowReputationThresholds() {
        user.setReputation(0);
        assertEquals("Beginner", user.level());
        user.setReputation(50);
        assertEquals("Contributor", user.level());
        user.setReputation(150);
        assertEquals("Helper", user.level());
        user.setReputation(400);
        assertEquals("Expert", user.level());
    }
}
