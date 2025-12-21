package inu.codin.codin.domain.user.internal.inbound.dto;

public record CompleteProfileResponse(
        String email,
        String userId,
        String authorities
) {}