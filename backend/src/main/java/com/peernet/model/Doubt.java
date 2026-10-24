package com.peernet.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "doubts", indexes = {
        @Index(name = "idx_doubt_status", columnList = "status"),
        @Index(name = "idx_doubt_subject", columnList = "subject_id"),
        @Index(name = "idx_doubt_asker", columnList = "asker_id")})
@Getter @Setter @NoArgsConstructor
public class Doubt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "asker_id")
    private AppUser asker;
    @ManyToOne(optional = false) @JoinColumn(name = "subject_id")
    private Subject subject;
    @Column(nullable = false, length = 200)
    private String title;
    @Column(nullable = false, length = 4000)
    private String description;
    @Column(length = 200)
    private String tags;
    @Column(length = 4000)
    private String codeSnippet;
    private boolean anonymous;
    private boolean urgent;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private DoubtStatus status = DoubtStatus.OPEN;
    private int escalationLevel = 0;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime lastAssignedAt;
    private LocalDateTime firstAnsweredAt;
    private LocalDateTime resolvedAt;
}
