package com.peernet.repository;

import com.peernet.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByResolvedFalseOrderByCreatedAtDesc();
}
