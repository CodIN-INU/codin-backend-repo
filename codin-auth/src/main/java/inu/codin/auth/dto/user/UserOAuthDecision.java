package inu.codin.auth.dto.user;


/**
 * USER fact 응답:
 * - userStatus: 유저 상태 사실
 * - isNewUser: 이번 요청에서 신규 생성 여부
 * - profileCompleted: 프로필 완료 여부
 * - tokenSubject: 토큰 subject로 쓸 식별자(email/sub)
 */
public record UserOAuthDecision(
        String userStatus,        // "ACTIVE" | "DISABLED" | "SUSPENDED"
        boolean isNewUser,
        boolean profileCompleted,
        String tokenSubject
) {}
