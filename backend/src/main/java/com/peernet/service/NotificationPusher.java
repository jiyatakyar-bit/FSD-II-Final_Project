package com.peernet.service;

import com.peernet.dto.Dtos.NotificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/** Pushes notifications to the browser on a background thread so the main request is never delayed. */
@Component
@RequiredArgsConstructor
public class NotificationPusher {
    private final SimpMessagingTemplate template;

    @Async
    public void push(Long userId, NotificationDto dto) {
        template.convertAndSendToUser(String.valueOf(userId), "/queue/notifications", dto);
    }
}
