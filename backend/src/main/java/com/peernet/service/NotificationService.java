package com.peernet.service;

import com.peernet.dto.Dtos.NotificationDto;
import com.peernet.model.AppUser;
import com.peernet.model.Notification;
import com.peernet.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository repository;
    private final NotificationPusher pusher;

    public static NotificationDto toDto(Notification n) {
        return new NotificationDto(n.getId(), n.getType(), n.getMessage(), n.getDoubtId(), n.isSeen(), n.getCreatedAt());
    }

    /** Saves a notification and pushes it live over WebSocket. */
    @Transactional
    public void notify(AppUser user, String type, String message, Long doubtId) {
        Notification n = new Notification();
        n.setUser(user);
        n.setType(type);
        n.setMessage(message.length() > 500 ? message.substring(0, 500) : message);
        n.setDoubtId(doubtId);
        n = repository.save(n);
        pusher.push(user.getId(), toDto(n));
    }

    public List<NotificationDto> latest(Long userId) {
        return repository.findTop50ByUserIdOrderByCreatedAtDesc(userId).stream().map(NotificationService::toDto).toList();
    }

    public long unreadCount(Long userId) {
        return repository.countByUserIdAndSeenFalse(userId);
    }

    @Transactional
    public void markAllRead(Long userId) {
        List<Notification> unread = repository.findByUserIdAndSeenFalse(userId);
        unread.forEach(n -> n.setSeen(true));
        repository.saveAll(unread);
    }
}
