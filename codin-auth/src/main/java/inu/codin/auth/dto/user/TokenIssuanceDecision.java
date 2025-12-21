package inu.codin.auth.dto.user;

public record TokenIssuanceDecision(
    String email,
    String userId,
    String authorities
) {}