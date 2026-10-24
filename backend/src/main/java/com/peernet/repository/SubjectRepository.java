package com.peernet.repository;

import com.peernet.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    boolean existsByNameIgnoreCase(String name);
    List<Subject> findAllByOrderByNameAsc();
}
