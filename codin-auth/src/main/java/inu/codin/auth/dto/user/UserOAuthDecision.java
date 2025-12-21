package inu.codin.auth.dto.user;


/**
 * USER fact 응답:
 * - userStatus: 유저 상태 사실
 * - isNewUser: 이번 요청에서 신규 생성 여부
 * - profileCompleted: 프로필 완료 여부
 * - tokenSubject: 토큰 subject로 쓸 식별자(email/sub)
 * - userId: MongoDB ObjectId (토큰 발급용) 
 * - authorities: 사용자 권한 정보 (토큰 발급용)
 */
public record UserOAuthDecision(
        String userStatus,        // "ACTIVE" | "DISABLED" | "SUSPENDED"
        boolean isNewUser,
        boolean profileCompleted,
        String tokenSubject,      // email/sub
        String userId,            // MongoDB ObjectId (HexString) 
        String authorities        // "ROLE_USER", "ROLE_ADMIN" 등
) {
    public TokenIssuanceDecision toTokenDecision() {
        return new TokenIssuanceDecision(tokenSubject, userId, authorities);
    }
}
