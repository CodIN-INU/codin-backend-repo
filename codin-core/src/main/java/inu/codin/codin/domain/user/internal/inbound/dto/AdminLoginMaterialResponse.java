package inu.codin.codin.domain.user.internal.dto;

public record AdminLoginMaterialResponse(
        String email,
        String encodedPassword
) {}