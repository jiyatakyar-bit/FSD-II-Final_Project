package com.peernet.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Getter @Setter @NoArgsConstructor
public class Report {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "answer_id")
    private Answer answer;
    @ManyToOne(optional = false) @JoinColumn(name = "reporter_id")
    private AppUser reporter;
    @Column(nullable = false, length = 500)
    private String reason;
    private boolean resolved;
    private LocalDateTime createdAt = LocalDateTime.now();
}
