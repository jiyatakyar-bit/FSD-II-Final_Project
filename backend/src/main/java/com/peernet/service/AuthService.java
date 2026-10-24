package com.peernet.service;

import com.peernet.config.ApiException;
import com.peernet.dto.Dtos.AuthResponse;
import com.peernet.dto.Dtos.LoginRequest;
import com.peernet.dto.Dtos.RegisterRequest;
import com.peernet.model.AppUser;
import com.peernet.model.Role;
import com.peernet.repository.UserRepository;
import com.peernet.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final DtoMapper mapper;

    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(409, "This email is already registered");
        }
        AppUser u = new AppUser();
        u.setName(req.name().trim());
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        u.setRole(Role.STUDENT);
        u = userRepository.save(u);
        return new AuthResponse(jwtService.generate(u), mapper.user(u));
    }

    public AuthResponse login(LoginRequest req) {
        AppUser u = userRepository.findByEmail(req.email().trim().toLowerCase())
                .filter(x -> passwordEncoder.matches(req.password(), x.getPasswordHash()))
                .orElseThrow(() -> new ApiException(401, "Invalid email or password"));
        if (!u.isActive()) {
            throw new ApiException(403, "This account has been disabled");
        }
        return new AuthResponse(jwtService.generate(u), mapper.user(u));
    }
}
