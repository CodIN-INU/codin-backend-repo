package inu.codin.codin.domain.user.internal.inbound.dto;

public record OauthDecisionRequest(
    String provider,     // "APPLE" | "GOOGLE"
    String identifier,   // email/sub 등 auth가 결정한 식별자
    String name,
    String department
) {}