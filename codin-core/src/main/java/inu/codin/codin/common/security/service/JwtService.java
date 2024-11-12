package inu.codin.codin.common.security.service;

import inu.codin.codin.common.security.exception.JwtException;
import inu.codin.codin.common.security.exception.SecurityErrorCode;
import inu.codin.codin.common.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * JWT 토큰 관련 비즈니스 로직을 처리하는 서비스
 *
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class JwtService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisStorageService redisStorageService;

    /**
     * 최초 로그인 시 Access Token, Refresh Token 발급
     * @param response
     */
    public void reissueToken(HttpServletResponse response) {
        createBothToken(response);
    }

    /**
     * Reissue : Refresh Token을 이용하여 Access Token, Refresh Token 재발급
     */
    public void reissueToken(String refreshToken, HttpServletResponse response) {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new JwtException(SecurityErrorCode.INVALID_TOKEN, "Refresh Token이 유효하지 않습니다.");
        }
        createBothToken(response);
    }

    /**
     * Access Token, Refresh Token 생성
     */
    private void createBothToken(HttpServletResponse response) {
        // 새로운 Access Token 발급
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        JwtTokenProvider.TokenDto newToken = jwtTokenProvider.createToken(authentication);

        // 응답 헤더에 Access Token 추가
        response.setHeader("Authorization", "Bearer " + newToken.getAccessToken());

        // 쿠키에 새로운 Refresh Token 추가
        Cookie refreshTokenCookie = new Cookie("RefreshToken", newToken.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        response.addCookie(refreshTokenCookie);
    }

    /**
     * 로그아웃 - Refresh Token 삭제
     */
    public void deleteToken() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        redisStorageService.deleteRefreshToken(authentication.getName());
    }

}