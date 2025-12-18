package inu.codin.codin.domain.user.internal.inbound.dto;

public record AdminLoginMaterialResponse(
        String email,
        String encodedPassword
) {}