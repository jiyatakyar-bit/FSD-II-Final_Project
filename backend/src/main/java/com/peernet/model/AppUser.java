package com.peernet.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 80)
    private String name;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Role role = Role.STUDENT;
    private int reputation = 0;
    private int answersCount = 0;
    private long totalResponseMinutes = 0;
    private boolean active = true;
    private LocalDateTime createdAt = LocalDateTime.now();

    /** Helper level derived from reputation points. */
    public String level() {
        if (reputation < 50) return "Beginner";
        if (reputation < 150) return "Contributor";
        if (reputation < 400) return "Helper";
        return "Expert";
    }
}
