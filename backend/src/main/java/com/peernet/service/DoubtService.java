package com.peernet.service;

import com.peernet.config.ApiException;
import com.peernet.config.RateLimiter;
import com.peernet.dto.Dtos.CreateDoubtRequest;
import com.peernet.dto.Dtos.DoubtDto;
import com.peernet.dto.Dtos.PageResponse;
import com.peernet.dto.Dtos.SimilarDto;
import com.peernet.dto.Dtos.UpdateDoubtRequest;
import com.peernet.model.AppUser;
import com.peernet.model.Doubt;
import com.peernet.model.DoubtAssignment;
import com.peernet.model.DoubtStatus;
import com.peernet.model.Role;
import com.peernet.model.Subject;
import com.peernet.repository.AnswerRepository;
import com.peernet.repository.DoubtAssignmentRepository;
import com.peernet.repository.DoubtRepository;
import com.peernet.repository.SubjectRepository;
import com.peernet.repository.UserRepository;
import com.peernet.security.CurrentUser;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DoubtService {
    private final DoubtRepository doubtRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final AnswerRepository answerRepository;
    private final DoubtAssignmentRepository assignmentRepository;
    private final MatchingService matchingService;
    private final DtoMapper mapper;
    private final RateLimiter rateLimiter;

    @Transactional
    public DoubtDto create(CurrentUser me, CreateDoubtRequest req) {
        rateLimiter.check(me.id(), "doubt", 5, 60_000);
        AppUser asker = userRepository.findById(me.id()).orElseThrow(() -> new ApiException(404, "User not found"));
        Subject subject = subjectRepository.findById(req.subjectId())
                .orElseThrow(() -> new ApiException(404, "Subject not found"));
        Doubt d = new Doubt();
        d.setAsker(asker);
        d.setSubject(subject);
        d.setTitle(req.title().trim());
        d.setDescription(req.description().trim());
        d.setTags(req.tags() == null ? null : req.tags().trim());
        d.setCodeSnippet(req.codeSnippet());
        d.setAnonymous(req.anonymous());
        d.setUrgent(req.urgent());
        d = doubtRepository.save(d);
        matchingService.assign(d);
        d = doubtRepository.save(d);
        return mapper.doubt(d, me, false);
    }

    /** Feed / knowledge base search with optional filters and pagination. */
    @Transactional(readOnly = true)
    public PageResponse<DoubtDto> search(CurrentUser me, Long subjectId, DoubtStatus status, String q,
                                         boolean mine, int page, int size) {
        Specification<Doubt> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (subjectId != null) p.add(cb.equal(root.get("subject").get("id"), subjectId));
            if (status != null) p.add(cb.equal(root.get("status"), status));
            if (mine) p.add(cb.equal(root.get("asker").get("id"), me.id()));
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                p.add(cb.or(cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like),
                        cb.like(cb.lower(root.get("tags")), like)));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
        int safeSize = Math.min(Math.max(size, 1), 50);
        PageRequest pageable = PageRequest.of(Math.max(page, 0), safeSize,
                Sort.by(Sort.Order.desc("urgent"), Sort.Order.desc("createdAt")));
        Page<Doubt> result = doubtRepository.findAll(spec, pageable);
        List<DoubtDto> items = result.getContent().stream().map(d -> mapper.doubt(d, me, false)).toList();
        return new PageResponse<>(items, result.getNumber(), result.getTotalPages(), result.getTotalElements());
    }

    /** Doubts that the matching algorithm has sent to this helper and that are still waiting. */
    @Transactional(readOnly = true)
    public List<DoubtDto> assignedTo(CurrentUser me) {
        List<DoubtStatus> waiting = List.of(DoubtStatus.ASSIGNED, DoubtStatus.OPEN);
        List<DoubtDto> out = new ArrayList<>();
        for (DoubtAssignment a : assignmentRepository.findByHelperIdAndDoubtStatusInOrderByAssignedAtDesc(me.id(), waiting)) {
            out.add(mapper.doubt(a.getDoubt(), me, false));
        }
        return out;
    }

    @Transactional(readOnly = true)
    public DoubtDto get(CurrentUser me, Long id) {
        return mapper.doubt(find(id), me, true);
    }

    /** Suggests resolved doubts with similar words in the title (duplicate detection while typing). */
    @Transactional(readOnly = true)
    public List<SimilarDto> similar(String title) {
        if (title == null || title.isBlank()) return List.of();
        List<String> words = new ArrayList<>();
        for (String w : title.toLowerCase().split("[^a-z0-9]+")) {
            if (w.length() >= 4) words.add(w);
        }
        words.sort((a, b) -> Integer.compare(b.length(), a.length()));
        Map<Long, SimilarDto> found = new LinkedHashMap<>();
        for (String w : words.stream().limit(3).toList()) {
            for (Doubt d : doubtRepository.findTop5ByStatusAndTitleContainingIgnoreCase(DoubtStatus.RESOLVED, w)) {
                found.putIfAbsent(d.getId(), new SimilarDto(d.getId(), d.getTitle(), d.getSubject().getName()));
            }
        }
        return found.values().stream().limit(5).toList();
    }

    @Transactional
    public DoubtDto update(CurrentUser me, Long id, UpdateDoubtRequest req) {
        Doubt d = findOwned(me, id);
        d.setTitle(req.title().trim());
        d.setDescription(req.description().trim());
        d.setTags(req.tags());
        return mapper.doubt(doubtRepository.save(d), me, true);
    }

    @Transactional
    public DoubtDto close(CurrentUser me, Long id) {
        Doubt d = findOwned(me, id);
        d.setStatus(DoubtStatus.CLOSED);
        return mapper.doubt(doubtRepository.save(d), me, true);
    }

    @Transactional
    public void delete(CurrentUser me, Long id) {
        Doubt d = findOwned(me, id);
        if (answerRepository.countByDoubtId(id) > 0) {
            throw new ApiException(409, "A doubt that already has answers cannot be deleted. Close it instead.");
        }
        assignmentRepository.deleteAll(assignmentRepository.findByDoubtId(id));
        doubtRepository.delete(d);
    }

    private Doubt find(Long id) {
        return doubtRepository.findById(id).orElseThrow(() -> new ApiException(404, "Doubt not found"));
    }

    private Doubt findOwned(CurrentUser me, Long id) {
        Doubt d = find(id);
        if (!d.getAsker().getId().equals(me.id()) && me.role() != Role.ADMIN) {
            throw new ApiException(403, "You can only change your own doubts");
        }
        return d;
    }
}
