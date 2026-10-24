package com.peernet.controller;

import com.peernet.dto.Dtos.DoubtDto;
import com.peernet.model.DoubtStatus;
import com.peernet.repository.DoubtRepository;
import com.peernet.security.CurrentUser;
import com.peernet.service.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Mentor panel: all unanswered doubts, most escalated first. */
@RestController
@RequestMapping("/api/mentor")
@RequiredArgsConstructor
public class MentorController {
    private final DoubtRepository doubtRepository;
    private final DtoMapper mapper;

    @GetMapping("/queue")
    @Transactional(readOnly = true)
    public List<DoubtDto> queue(@AuthenticationPrincipal CurrentUser me) {
        return doubtRepository.findByStatusInOrderByEscalationLevelDescCreatedAtAsc(
                        List.of(DoubtStatus.OPEN, DoubtStatus.ASSIGNED))
                .stream().map(d -> mapper.doubt(d, me, false)).toList();
    }
}
