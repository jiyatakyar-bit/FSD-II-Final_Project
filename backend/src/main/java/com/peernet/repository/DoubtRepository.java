package com.peernet.repository;

import com.peernet.model.Doubt;
import com.peernet.model.DoubtStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface DoubtRepository extends JpaRepository<Doubt, Long>, JpaSpecificationExecutor<Doubt> {
    List<Doubt> findByStatusIn(Collection<DoubtStatus> statuses);
    List<Doubt> findByStatusInOrderByEscalationLevelDescCreatedAtAsc(Collection<DoubtStatus> statuses);
    List<Doubt> findTop5ByStatusAndTitleContainingIgnoreCase(DoubtStatus status, String word);
    List<Doubt> findByFirstAnsweredAtNotNull();
    long countByStatus(DoubtStatus status);
    long countByStatusIn(Collection<DoubtStatus> statuses);

    @Query("select d.subject.name, count(d) from Doubt d group by d.subject.name order by count(d) desc")
    List<Object[]> countBySubject();
}
