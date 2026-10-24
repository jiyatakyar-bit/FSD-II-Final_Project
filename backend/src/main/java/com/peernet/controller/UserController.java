package com.peernet.controller;

import com.peernet.dto.Dtos.ExpertiseRequest;
import com.peernet.dto.Dtos.SubjectDto;
import com.peernet.dto.Dtos.UpdateProfileRequest;
import com.peernet.dto.Dtos.UserDto;
import com.peernet.security.CurrentUser;
import com.peernet.service.SubjectService;
import com.peernet.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final SubjectService subjectService;

    @GetMapping("/api/users/me")
    public UserDto me(@AuthenticationPrincipal CurrentUser me) {
        return userService.me(me.id());
    }

    @PutMapping("/api/users/me")
    public UserDto updateName(@AuthenticationPrincipal CurrentUser me, @Valid @RequestBody UpdateProfileRequest req) {
        return userService.updateName(me.id(), req.name());
    }

    @PutMapping("/api/users/me/expertise")
    public UserDto updateExpertise(@AuthenticationPrincipal CurrentUser me, @RequestBody ExpertiseRequest req) {
        return userService.updateExpertise(me.id(), req.subjectIds());
    }

    @GetMapping("/api/subjects")
    public List<SubjectDto> subjects() {
        return subjectService.list();
    }
}
