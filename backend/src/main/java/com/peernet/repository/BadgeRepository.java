package com.peernet.repository;

import com.peernet.model.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
    boolean existsByUserIdAndName(Long userId, String name);
    List<Badge> findByUserId(Long userId);
}
