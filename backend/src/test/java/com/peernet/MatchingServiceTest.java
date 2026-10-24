package com.peernet;

import com.peernet.dto.Dtos.MatchDto;
import com.peernet.model.AppUser;
import com.peernet.model.Doubt;
import com.peernet.model.HelperExpertise;
import com.peernet.model.Role;
import com.peernet.model.Subject;
import com.peernet.repository.DoubtAssignmentRepository;
import com.peernet.repository.HelperExpertiseRepository;
import com.peernet.repository.UserRepository;
import com.peernet.service.MatchingService;
import com.peernet.service.NotificationService;
import com.peernet.service.SettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {
    @Mock UserRepository userRepository;
    @Mock HelperExpertiseRepository expertiseRepository;
    @Mock DoubtAssignmentRepository assignmentRepository;
    @Mock SettingsService settings;
    @Mock NotificationService notificationService;
    @InjectMocks MatchingService matchingService;

    Doubt doubt;
    AppUser expert;
    AppUser newbie;

    @BeforeEach
    void setUp() {
        Subject subject = new Subject();
        subject.setId(1L);
        subject.setName("Java");
        AppUser asker = user(99L, "Asker", 0);
        doubt = new Doubt();
        doubt.setId(5L);
        doubt.setAsker(asker);
        doubt.setSubject(subject);

        expert = user(1L, "Expert", 100);
        newbie = user(2L, "Newbie", 10);

        when(settings.getDouble("w_topic")).thenReturn(0.40);
        when(settings.getDouble("w_reputation")).thenReturn(0.25);
        when(settings.getDouble("w_availability")).thenReturn(0.20);
        when(settings.getDouble("w_responsiveness")).thenReturn(0.15);
        when(userRepository.findByRoleInAndActiveTrue(anyCollection())).thenReturn(List.of(asker, expert, newbie));
        lenient().when(assignmentRepository.countByHelperIdAndDoubtStatusIn(anyLong(), anyCollection())).thenReturn(0L);
        when(expertiseRepository.findByUserIdAndSubjectId(anyLong(), anyLong())).thenReturn(Optional.empty());
    }

    private AppUser user(Long id, String name, int reputation) {
        AppUser u = new AppUser();
        u.setId(id);
        u.setName(name);
        u.setRole(Role.STUDENT);
        u.setReputation(reputation);
        return u;
    }

    @Test
    void askerIsNeverRecommendedAsHelper() {
        List<MatchDto> ranked = matchingService.rank(doubt, false);
        assertEquals(2, ranked.size());
        assertTrue(ranked.stream().noneMatch(m -> m.userId().equals(99L)));
    }

    @Test
    void higherReputationRanksFirstWhenOtherFactorsAreEqual() {
        List<MatchDto> ranked = matchingService.rank(doubt, false);
        assertEquals("Expert", ranked.get(0).name());
    }

    @Test
    void declaredSubjectExpertiseBoostsTheScore() {
        HelperExpertise e = new HelperExpertise();
        e.setDeclared(true);
        e.setProvenCount(5);
        when(expertiseRepository.findByUserIdAndSubjectId(2L, 1L)).thenReturn(Optional.of(e));

        List<MatchDto> ranked = matchingService.rank(doubt, false);
        MatchDto top = ranked.get(0);
        assertEquals("Newbie", top.name());
        assertEquals(1.0, top.topic());
    }

    @Test
    void scoreFollowsTheWeightedFormula() {
        List<MatchDto> ranked = matchingService.rank(doubt, false);
        MatchDto m = ranked.get(0); // Expert: topic 0, rep 1.0, availability 1.0, responsiveness 0.5
        double expected = 0.40 * 0 + 0.25 * 1.0 + 0.20 * 1.0 + 0.15 * 0.5;
        assertEquals(MatchingService.round(expected), m.score(), 0.0001);
    }
}
