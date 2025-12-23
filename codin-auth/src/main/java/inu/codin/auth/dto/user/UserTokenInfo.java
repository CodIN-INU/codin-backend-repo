package inu.codin.auth.dto.user;

/**
 * 토큰 생성/재발급을 위한 사용자 정보
 * Auth 서버가 토큰에 포함할 최소한의 정보만 포함
 */
public record UserTokenInfo(
        String email,
        String userId,
        String authorities
) {}