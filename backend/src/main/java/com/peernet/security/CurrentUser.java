package com.peernet.security;

import com.peernet.model.Role;

/** Authenticated user stored in the Spring Security context. */
public record CurrentUser(Long id, String email, Role role) {}
