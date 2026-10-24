package com.peernet.controller;

import com.peernet.dto.Dtos.NotificationDto;
import com.peernet.security.CurrentUser;
import com.peernet.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationDto> list(@AuthenticationPrincipal CurrentUser me) {
        return notificationService.latest(me.id());
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unread(@AuthenticationPrincipal CurrentUser me) {
        return Map.of("count", notificationService.unreadCount(me.id()));
    }

    @PostMapping("/read-all")
    public void readAll(@AuthenticationPrincipal CurrentUser me) {
        notificationService.markAllRead(me.id());
    }
}
