package com.peernet.controller;

import com.peernet.dto.Dtos.AnswerDto;
import com.peernet.dto.Dtos.AnswerRequest;
import com.peernet.dto.Dtos.CreateDoubtRequest;
import com.peernet.dto.Dtos.DoubtDto;
import com.peernet.dto.Dtos.PageResponse;
import com.peernet.dto.Dtos.SimilarDto;
import com.peernet.dto.Dtos.UpdateDoubtRequest;
import com.peernet.model.DoubtStatus;
import com.peernet.security.CurrentUser;
import com.peernet.service.AnswerService;
import com.peernet.service.DoubtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/doubts")
@RequiredArgsConstructor
public class DoubtController {
    private final DoubtService doubtService;
    private final AnswerService answerService;

    @PostMapping
    public DoubtDto create(@AuthenticationPrincipal CurrentUser me, @Valid @RequestBody CreateDoubtRequest req) {
        return doubtService.create(me, req);
    }

    @GetMapping
    public PageResponse<DoubtDto> search(@AuthenticationPrincipal CurrentUser me,
                                         @RequestParam(required = false) Long subjectId,
                                         @RequestParam(required = false) DoubtStatus status,
                                         @RequestParam(required = false) String q,
                                         @RequestParam(defaultValue = "false") boolean mine,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        return doubtService.search(me, subjectId, status, q, mine, page, size);
    }

    @GetMapping("/assigned")
    public List<DoubtDto> assigned(@AuthenticationPrincipal CurrentUser me) {
        return doubtService.assignedTo(me);
    }

    @GetMapping("/similar")
    public List<SimilarDto> similar(@RequestParam String title) {
        return doubtService.similar(title);
    }

    @GetMapping("/{id}")
    public DoubtDto get(@AuthenticationPrincipal CurrentUser me, @PathVariable Long id) {
        return doubtService.get(me, id);
    }

    @PutMapping("/{id}")
    public DoubtDto update(@AuthenticationPrincipal CurrentUser me, @PathVariable Long id,
                           @Valid @RequestBody UpdateDoubtRequest req) {
        return doubtService.update(me, id, req);
    }

    @PostMapping("/{id}/close")
    public DoubtDto close(@AuthenticationPrincipal CurrentUser me, @PathVariable Long id) {
        return doubtService.close(me, id);
    }

    @DeleteMapping("/{id}")
    public void delete(@AuthenticationPrincipal CurrentUser me, @PathVariable Long id) {
        doubtService.delete(me, id);
    }

    @PostMapping("/{id}/answers")
    public AnswerDto answer(@AuthenticationPrincipal CurrentUser me, @PathVariable Long id,
                            @Valid @RequestBody AnswerRequest req) {
        return answerService.post(me, id, req.content());
    }
}
