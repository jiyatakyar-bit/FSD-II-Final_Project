package com.peernet.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "doubt_assignments", indexes = @Index(name = "idx_assign_helper", columnList = "helper_id"))
@Getter @Setter @NoArgsConstructor
public class DoubtAssignment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "doubt_id")
    private Doubt doubt;
    @ManyToOne(optional = false) @JoinColumn(name = "helper_id")
    private AppUser helper;
    private int roundNo;
    private double score;
    private LocalDateTime assignedAt = LocalDateTime.now();
}
