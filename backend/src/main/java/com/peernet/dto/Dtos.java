package com.peernet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** All request / response objects used by the REST API. */
public final class Dtos {
    private Dtos() {}

    // ---------- auth & users ----------
    public record RegisterRequest(@NotBlank @Size(max = 80) String name,
                                  @NotBlank @Email String email,
                                  @NotBlank @Size(min = 6, max = 100) String password) {}
    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}
    public record UserDto(Long id, String name, String email, String role, int reputation, String level,
                          List<String> badges, List<Long> expertiseSubjectIds) {}
    public record AuthResponse(String token, UserDto user) {}
    public record UpdateProfileRequest(@NotBlank @Size(max = 80) String name) {}
    public record ExpertiseRequest(List<Long> subjectIds) {}

    // ---------- subjects ----------
    public record SubjectDto(Long id, String name) implements Serializable {}
    public record SubjectRequest(@NotBlank @Size(max = 80) String name) {}

    // ---------- doubts & answers ----------
    public record CreateDoubtRequest(@NotNull Long subjectId,
                                     @NotBlank @Size(max = 200) String title,
                                     @NotBlank @Size(max = 4000) String description,
                                     @Size(max = 200) String tags,
                                     @Size(max = 4000) String codeSnippet,
                                     boolean anonymous, boolean urgent) {}
    public record UpdateDoubtRequest(@NotBlank @Size(max = 200) String title,
                                     @NotBlank @Size(max = 4000) String description,
                                     @Size(max = 200) String tags) {}
    public record AnswerRequest(@NotBlank @Size(max = 4000) String content) {}
    public record VoteRequest(int value) {}
    public record ReportRequest(@NotBlank @Size(max = 500) String reason) {}

    public record AnswerDto(Long id, String content, Long helperId, String helperName, String helperLevel,
                            int helperReputation, int votes, boolean accepted, boolean verified,
                            LocalDateTime createdAt, int myVote) {}
    public record DoubtDto(Long id, String title, String description, String tags, String codeSnippet,
                           Long subjectId, String subjectName, Long askerId, String askerName,
                           boolean mine, boolean anonymous, boolean urgent, String status,
                           int escalationLevel, long answerCount, LocalDateTime createdAt,
                           List<AnswerDto> answers) {}
    public record SimilarDto(Long id, String title, String subjectName) {}
    public record PageResponse<T>(List<T> items, int page, int totalPages, long totalItems) {}

    // ---------- notifications & leaderboard ----------
    public record NotificationDto(Long id, String type, String message, Long doubtId, boolean seen,
                                  LocalDateTime createdAt) {}
    public record LeaderboardEntry(Long userId, String name, int points, String level) implements Serializable {}

    // ---------- admin ----------
    public record RoleRequest(@NotBlank String role) {}
    public record ActiveRequest(boolean active) {}
    public record ResolveReportRequest(boolean uphold) {}
    public record AdminUserDto(Long id, String name, String email, String role, int reputation, boolean active) {}
    public record ReportDto(Long id, Long answerId, Long doubtId, String answerContent, String reporterName,
                            String reason, LocalDateTime createdAt) {}
    public record MatchDto(Long userId, String name, String role, double topic, double reputation,
                           double availability, double responsiveness, double score) {}
    public record StatsDto(long totalUsers, long totalDoubts, long openDoubts, long resolvedDoubts,
                           long totalAnswers, double resolutionRate, double avgFirstResponseMinutes,
                           Map<String, Long> doubtsBySubject, List<LeaderboardEntry> topHelpers) {}
}
