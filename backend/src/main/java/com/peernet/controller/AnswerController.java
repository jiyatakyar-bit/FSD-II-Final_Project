package com.peernet.controller;

import com.peernet.dto.Dtos.AnswerDto;
import com.peernet.dto.Dtos.ReportRequest;
import com.peernet.dto.Dtos.VoteRequest;
import com.peernet.security.CurrentUser;
import com.peernet.service.AnswerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/answers")
@RequiredArgsConstructor
public class AnswerController {
    private final AnswerService answerService;

    @PostMapping("/{id}/vote")
    public AnswerDto vote(@AuthenticationPrincipal CurrentUser me, @PathVariable Long id,
                          @RequestBody VoteRequest req) {
        return answerService.vote(me, id, req.value());
    }

    @PostMapping("/{id}/accept")
    public AnswerDto accept(@AuthenticationPrincipal CurrentUser me, @PathVariable Long id) {
        return answerService.accept(me, id);
    }

    @PostMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('MENTOR','ADMIN')")
    public AnswerDto verify(@AuthenticationPrincipal CurrentUser me, @PathVariable Long id) {
        return answerService.verify(me, id);
    }

    @PostMapping("/{id}/report")
    public void report(@AuthenticationPrincipal CurrentUser me, @PathVariable Long id,
                       @Valid @RequestBody ReportRequest req) {
        answerService.report(me, id, req.reason());
    }
}
