package inu.codin.auth.dto.user;

public record CompleteProfileResponse(
    String email,
    String userId,
    String authorities
) {
    public TokenIssuanceDecision toTokenDecision() {
        return new TokenIssuanceDecision(email, userId, authorities);
    }
}
