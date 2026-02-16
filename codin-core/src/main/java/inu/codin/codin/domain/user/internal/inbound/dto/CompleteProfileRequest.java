package inu.codin.codin.domain.user.internal.inbound.dto;

public record CompleteProfileRequest(
        String email,
        String nickname
) {}