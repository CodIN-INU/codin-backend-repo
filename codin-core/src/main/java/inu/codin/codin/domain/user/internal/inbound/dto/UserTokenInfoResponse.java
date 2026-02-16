package inu.codin.codin.domain.user.internal.inbound.dto;

/**
 * 토큰 생성/재발급을 위한 사용자 정보
 * Auth 서버가 토큰에 포함할 최소한의 정보만 포함
 */
public record UserTokenInfoResponse(
        String email,       // 사용자 이메일
        String userId,      // MongoDB ObjectId (HexString)
        String authorities  // 권한 정보 (예: "ROLE_USER", "ROLE_ADMIN")
) {}
