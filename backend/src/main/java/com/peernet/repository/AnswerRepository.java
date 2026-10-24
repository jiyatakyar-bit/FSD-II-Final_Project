package com.peernet.repository;

import com.peernet.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByDoubtIdOrderByAcceptedDescVotesDescCreatedAtAsc(Long doubtId);
    long countByDoubtId(Long doubtId);
    long countByHelperId(Long helperId);
    List<Answer> findByHelperIdOrderByCreatedAtDesc(Long helperId);
}
