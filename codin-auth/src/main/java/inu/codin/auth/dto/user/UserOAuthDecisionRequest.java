package inu.codin.auth.dto.user;

public record UserOAuthDecisionRequest (
        String provider,
        String identifier,
        String name,
        String department
) {}