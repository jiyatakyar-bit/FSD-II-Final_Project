package com.peernet.service;

import com.peernet.model.AppUser;
import com.peernet.model.Badge;
import com.peernet.model.ReputationLog;
import com.peernet.repository.BadgeRepository;
import com.peernet.repository.ReputationLogRepository;
import com.peernet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReputationService {
    private final UserRepository userRepository;
    private final ReputationLogRepository logRepository;
    private final BadgeRepository badgeRepository;
    private final NotificationService notificationService;

    /** Adds (or removes) reputation points, writes the log entry and checks for new badges. */
    @Transactional
    @CacheEvict(value = "leaderboard", allEntries = true)
    public void award(AppUser user, String event, int points) {
        if (points == 0) return;
        user.setReputation(Math.max(0, user.getReputation() + points));
        userRepository.save(user);

        ReputationLog log = new ReputationLog();
        log.setUser(user);
        log.setEvent(event);
        log.setPoints(points);
        logRepository.save(log);

        if (user.getReputation() >= 100) {
            grantBadge(user, "Rising Star");
        }
    }

    @Transactional
    public void grantBadge(AppUser user, String badgeName) {
        if (badgeRepository.existsByUserIdAndName(user.getId(), badgeName)) return;
        Badge badge = new Badge();
        badge.setUser(user);
        badge.setName(badgeName);
        badgeRepository.save(badge);
        notificationService.notify(user, "BADGE", "You earned a new badge: " + badgeName, null);
    }
}
