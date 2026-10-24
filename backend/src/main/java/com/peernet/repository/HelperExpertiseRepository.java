package com.peernet.repository;

import com.peernet.model.HelperExpertise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HelperExpertiseRepository extends JpaRepository<HelperExpertise, Long> {
    List<HelperExpertise> findByUserId(Long userId);
    Optional<HelperExpertise> findByUserIdAndSubjectId(Long userId, Long subjectId);
}
