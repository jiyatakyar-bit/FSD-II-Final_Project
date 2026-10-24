package com.peernet.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "answers", indexes = @Index(name = "idx_answer_doubt", columnList = "doubt_id"))
@Getter @Setter @NoArgsConstructor
public class Answer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "doubt_id")
    private Doubt doubt;
    @ManyToOne(optional = false) @JoinColumn(name = "helper_id")
    private AppUser helper;
    @Column(nullable = false, length = 4000)
    private String content;
    private int votes = 0;
    private boolean accepted;
    private boolean verified;
    private LocalDateTime createdAt = LocalDateTime.now();
}
