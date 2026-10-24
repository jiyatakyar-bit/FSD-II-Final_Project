package com.peernet.service;

import com.peernet.config.ApiException;
import com.peernet.dto.Dtos.SubjectDto;
import com.peernet.model.Subject;
import com.peernet.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectService {
    private final SubjectRepository repository;

    /** Subjects rarely change, so the list is cached (Redis when enabled). */
    @Cacheable("subjects")
    public List<SubjectDto> list() {
        List<SubjectDto> out = new ArrayList<>();
        repository.findAllByOrderByNameAsc().forEach(s -> out.add(new SubjectDto(s.getId(), s.getName())));
        return out;
    }

    @CacheEvict(value = "subjects", allEntries = true)
    public SubjectDto create(String name) {
        String trimmed = name.trim();
        if (repository.existsByNameIgnoreCase(trimmed)) {
            throw new ApiException(409, "Subject already exists");
        }
        Subject s = new Subject();
        s.setName(trimmed);
        s = repository.save(s);
        return new SubjectDto(s.getId(), s.getName());
    }

    @CacheEvict(value = "subjects", allEntries = true)
    public void delete(Long id) {
        repository.deleteById(id);
        repository.flush();
    }
}
