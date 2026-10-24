package com.peernet.service;

import com.peernet.dto.Dtos.LeaderboardEntry;
import com.peernet.model.AppUser;
import com.peernet.repository.ReputationLogRepository;
import com.peernet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Leaderboards are read very often and change rarely, so they are cached (and cleared when reputation changes). */
@Service
@RequiredArgsConstructor
public class LeaderboardService {
    private final UserRepository userRepository;
    private final ReputationLogRepository logRepository;

    @Cacheable(value = "leaderboard", key = "'overall'")
    public List<LeaderboardEntry> overall() {
        List<LeaderboardEntry> out = new ArrayList<>();
        for (AppUser u : userRepository.findTop10ByActiveTrueOrderByReputationDesc()) {
            out.add(new LeaderboardEntry(u.getId(), u.getName(), u.getReputation(), u.level()));
        }
        return out;
    }

    @Cacheable(value = "leaderboard", key = "'weekly'")
    public List<LeaderboardEntry> weekly() {
        List<LeaderboardEntry> out = new ArrayList<>();
        for (Object[] row : logRepository.weeklyTotals(LocalDateTime.now().minusDays(7))) {
            int points = ((Number) row[2]).intValue();
            if (points <= 0 || out.size() >= 10) continue;
            Long userId = ((Number) row[0]).longValue();
            String level = userRepository.findById(userId).map(AppUser::level).orElse("Beginner");
            out.add(new LeaderboardEntry(userId, (String) row[1], points, level));
        }
        return out;
    }
}
