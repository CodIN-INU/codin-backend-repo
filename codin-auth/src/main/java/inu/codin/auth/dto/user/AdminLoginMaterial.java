package inu.codin.auth.dto.user;

public record AdminLoginMaterial(
    String email,
    String encodedPassword,
    String userId,
    String authorities
) {
    public TokenIssuanceDecision toTokenDecision() {
        return new TokenIssuanceDecision(email, userId, authorities);
    }
}
