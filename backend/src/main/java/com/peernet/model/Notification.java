package com.peernet.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = @Index(name = "idx_notif_user", columnList = "user_id"))
@Getter @Setter @NoArgsConstructor
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "user_id")
    private AppUser user;
    @Column(nullable = false, length = 30)
    private String type;
    @Column(nullable = false, length = 500)
    private String message;
    private Long doubtId;
    private boolean seen;
    private LocalDateTime createdAt = LocalDateTime.now();
}
