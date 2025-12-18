package inu.codin.codin.domain.user.internal.dto;

public record CompleteProfileRequest(
        String email,
        String nickname
) {}