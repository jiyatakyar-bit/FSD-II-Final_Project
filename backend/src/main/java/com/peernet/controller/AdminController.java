package com.peernet.controller;

import com.peernet.dto.Dtos.ActiveRequest;
import com.peernet.dto.Dtos.AdminUserDto;
import com.peernet.dto.Dtos.MatchDto;
import com.peernet.dto.Dtos.ReportDto;
import com.peernet.dto.Dtos.ResolveReportRequest;
import com.peernet.dto.Dtos.RoleRequest;
import com.peernet.dto.Dtos.StatsDto;
import com.peernet.dto.Dtos.SubjectDto;
import com.peernet.dto.Dtos.SubjectRequest;
import com.peernet.service.AdminService;
import com.peernet.service.SettingsService;
import com.peernet.service.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** All endpoints here require the ADMIN role (configured in SecurityConfig). */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final SettingsService settingsService;
    private final SubjectService subjectService;

    @GetMapping("/stats")
    public StatsDto stats() {
        return adminService.stats();
    }

    @GetMapping("/settings")
    public Map<String, String> settings() {
        return settingsService.all();
    }

    @PutMapping("/settings")
    public Map<String, String> updateSettings(@RequestBody Map<String, String> changes) {
        return settingsService.update(changes);
    }

    @GetMapping("/users")
    public List<AdminUserDto> users() {
        return adminService.users();
    }

    @PutMapping("/users/{id}/role")
    public void role(@PathVariable Long id, @Valid @RequestBody RoleRequest req) {
        adminService.setRole(id, req.role());
    }

    @PutMapping("/users/{id}/active")
    public void active(@PathVariable Long id, @RequestBody ActiveRequest req) {
        adminService.setActive(id, req.active());
    }

    @PostMapping("/subjects")
    public SubjectDto createSubject(@Valid @RequestBody SubjectRequest req) {
        return subjectService.create(req.name());
    }

    @DeleteMapping("/subjects/{id}")
    public void deleteSubject(@PathVariable Long id) {
        subjectService.delete(id);
    }

    @GetMapping("/reports")
    public List<ReportDto> reports() {
        return adminService.reports();
    }

    @PostMapping("/reports/{id}/resolve")
    public void resolve(@PathVariable Long id, @RequestBody ResolveReportRequest req) {
        adminService.resolveReport(id, req.uphold());
    }

    @GetMapping("/doubts/{id}/matching")
    public List<MatchDto> matching(@PathVariable Long id) {
        return adminService.matching(id);
    }
}
