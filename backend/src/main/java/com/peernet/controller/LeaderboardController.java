package com.peernet.controller;

import com.peernet.dto.Dtos.LeaderboardEntry;
import com.peernet.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LeaderboardController {
    private final LeaderboardService leaderboardService;

    @GetMapping("/api/leaderboard")
    public List<LeaderboardEntry> leaderboard(@RequestParam(defaultValue = "overall") String type) {
        return "weekly".equalsIgnoreCase(type) ? leaderboardService.weekly() : leaderboardService.overall();
    }
}
