package inu.codin.codin.domain.user.internal.inbound.dto;

public record OauthDecisionResponse(
    String userStatus,
    boolean isNewUser,
    boolean profileCompleted,
    String tokenSubject,
    String userId,
    String authorities
) {}