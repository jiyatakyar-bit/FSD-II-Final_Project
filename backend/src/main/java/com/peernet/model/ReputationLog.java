package com.peernet.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "reputation_logs", indexes = @Index(name = "idx_replog_user", columnList = "user_id"))
@Getter @Setter @NoArgsConstructor
public class ReputationLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "user_id")
    private AppUser user;
    @Column(nullable = false, length = 40)
    private String event;
    private int points;
    private LocalDateTime createdAt = LocalDateTime.now();
}
