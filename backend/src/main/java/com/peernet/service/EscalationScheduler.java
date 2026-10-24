package com.peernet.service;

import com.peernet.model.Doubt;
import com.peernet.model.DoubtStatus;
import com.peernet.repository.DoubtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** Every minute: unanswered doubts are sent to the next set of helpers (mentors from level 2). */
@Component
@RequiredArgsConstructor
public class EscalationScheduler {
    private static final int MAX_LEVEL = 3;
    private final DoubtRepository doubtRepository;
    private final MatchingService matchingService;
    private final SettingsService settings;

    @Scheduled(fixedDelay = 60_000, initialDelay = 60_000)
    @Transactional
    public void escalate() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes((long) settings.getDouble("escalation_minutes"));
        List<Doubt> waiting = doubtRepository.findByStatusIn(List.of(DoubtStatus.OPEN, DoubtStatus.ASSIGNED));
        for (Doubt d : waiting) {
            LocalDateTime since = d.getLastAssignedAt() != null ? d.getLastAssignedAt() : d.getCreatedAt();
            if (since.isBefore(cutoff) && d.getEscalationLevel() < MAX_LEVEL) {
                d.setEscalationLevel(d.getEscalationLevel() + 1);
                matchingService.assign(d);
                doubtRepository.save(d);
            }
        }
    }
}
