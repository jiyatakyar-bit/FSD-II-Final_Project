package com.peernet.service;

import com.peernet.config.ApiException;
import com.peernet.dto.Dtos.UserDto;
import com.peernet.model.AppUser;
import com.peernet.model.HelperExpertise;
import com.peernet.model.Subject;
import com.peernet.repository.HelperExpertiseRepository;
import com.peernet.repository.SubjectRepository;
import com.peernet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final HelperExpertiseRepository expertiseRepository;
    private final DtoMapper mapper;

    public UserDto me(Long id) {
        return mapper.user(find(id));
    }

    @Transactional
    public UserDto updateName(Long id, String name) {
        AppUser u = find(id);
        u.setName(name.trim());
        return mapper.user(userRepository.save(u));
    }

    /** Replaces the list of subjects the user wants to help in (accepted-answer counts are kept). */
    @Transactional
    public UserDto updateExpertise(Long id, List<Long> subjectIds) {
        AppUser u = find(id);
        Set<Long> wanted = new HashSet<>(subjectIds == null ? List.of() : subjectIds);
        for (HelperExpertise e : expertiseRepository.findByUserId(id)) {
            e.setDeclared(wanted.contains(e.getSubject().getId()));
            expertiseRepository.save(e);
            wanted.remove(e.getSubject().getId());
        }
        for (Long subjectId : wanted) {
            Subject s = subjectRepository.findById(subjectId)
                    .orElseThrow(() -> new ApiException(404, "Subject not found"));
            HelperExpertise e = new HelperExpertise();
            e.setUser(u);
            e.setSubject(s);
            e.setDeclared(true);
            expertiseRepository.save(e);
        }
        return mapper.user(u);
    }

    private AppUser find(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ApiException(404, "User not found"));
    }
}
