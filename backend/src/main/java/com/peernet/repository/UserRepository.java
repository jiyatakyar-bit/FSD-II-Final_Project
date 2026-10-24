package com.peernet.repository;

import com.peernet.model.AppUser;
import com.peernet.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
    boolean existsByEmail(String email);
    List<AppUser> findByRoleInAndActiveTrue(Collection<Role> roles);
    List<AppUser> findTop10ByActiveTrueOrderByReputationDesc();
    List<AppUser> findAllByOrderByCreatedAtDesc();
}
