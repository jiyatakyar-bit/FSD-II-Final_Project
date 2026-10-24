package com.peernet.repository;

import com.peernet.model.DoubtAssignment;
import com.peernet.model.DoubtStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface DoubtAssignmentRepository extends JpaRepository<DoubtAssignment, Long> {
    List<DoubtAssignment> findByDoubtId(Long doubtId);
    long countByHelperIdAndDoubtStatusIn(Long helperId, Collection<DoubtStatus> statuses);
    List<DoubtAssignment> findByHelperIdAndDoubtStatusInOrderByAssignedAtDesc(Long helperId, Collection<DoubtStatus> statuses);
}
