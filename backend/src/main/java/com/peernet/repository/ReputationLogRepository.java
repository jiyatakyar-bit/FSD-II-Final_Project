package com.peernet.repository;

import com.peernet.model.ReputationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReputationLogRepository extends JpaRepository<ReputationLog, Long> {
    @Query("select l.user.id, l.user.name, sum(l.points) from ReputationLog l " +
           "where l.createdAt >= :since group by l.user.id, l.user.name order by sum(l.points) desc")
    List<Object[]> weeklyTotals(@Param("since") LocalDateTime since);
}
