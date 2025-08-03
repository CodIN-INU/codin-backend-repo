package inu.codin.codin.common.security.service;

import inu.codin.codin.common.security.exception.JwtException;
import inu.codin.codin.common.security.exception.SecurityErrorCode;
import inu.codin.codin.common.security.jwt.JwtAuthenticationToken;
import inu.codin.codin.common.security.jwt.JwtTokenProvider;
import inu.codin.codin.common.security.jwt.JwtUtils;
import inu.codin.codin.domain.user.security.CustomUserDetailsService;
import inu.codin.codin.infra.redis.RedisStorageService;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * JWT 토큰 관련 비즈니스 로직을 처리하는 서비스
 *
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class JwtService {

    private final RedisStorageService redisStorageService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;

    @Value("${server.domain}")
    private String BASERURL;

    private final String REFRESH_TOKEN = "x-refresh-token";
    private final String ACCESS_TOKEN = "Authorization";
    private final String ACCESS_TOKEN_PREFIX = "Bearer ";

    /**
     * 최초 로그인 시 Access Token, Refresh Token 발급
     * @param response
     */
    public void createToken(HttpServletResponse response) {
        createBothToken(response);
        log.info("[createToken] Access Token, Refresh Token 발급 완료");
    }

    /**
     * Refresh Token을 이용하여 Access Token, Refresh Token 재발급
     * @param request
     * @param response
     */
    public void reissueToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtUtils.getRefreshToken(request);

        if (refreshToken == null) {
            log.error("[reissueToken] Refresh Token이 없습니다.");
            throw new JwtException(SecurityErrorCode.INVALID_TOKEN, "Refresh Token이 없습니다.");
        }

        String username = jwtTokenProvider.getUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // 토큰이 유효하고, SecurityContext에 Authentication 객체가 없는 경우
        if (userDetails != null) {
            // Authentication 객체 생성 후 SecurityContext에 저장 (인증 완료)
            JwtAuthenticationToken authentication = new JwtAuthenticationToken(userDetails, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        reissueToken(refreshToken, response);
    }

    /**
     * Refresh Token을 이용하여 Access Token, Refresh Token 재발급
     * @param refreshToken
     * @param response
     */
    public void reissueToken(String refreshToken, HttpServletResponse response) {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            log.error("[reissueToken] Refresh Token이 유효하지 않습니다. : {}", refreshToken);
            throw new JwtException(SecurityErrorCode.INVALID_TOKEN, "Refresh Token이 유효하지 않습니다.");
        }

        createBothToken(response);
        log.info("[reissueToken] Access Token, Refresh Token 재발급 완료");
    }

    /**
     * Access Token, Refresh Token 생성
     */
    private void createBothToken(HttpServletResponse response) {
        // 새로운 Access Token 발급
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        JwtTokenProvider.TokenDto newToken = jwtTokenProvider.createToken(authentication);

        // Authorization 헤더에 Access Token 추가
        response.setHeader(ACCESS_TOKEN, ACCESS_TOKEN_PREFIX + newToken.getAccessToken());

        Cookie newAccessToken = new Cookie("x-access-token", newToken.getAccessToken());
        newAccessToken.setHttpOnly(true);
        newAccessToken.setSecure(true);
        newAccessToken.setPath("/");
        newAccessToken.setMaxAge(30 * 60); // 10분
        newAccessToken.setDomain(BASERURL.split("//")[1]);
        newAccessToken.setAttribute("SameSite", "None");
        response.addCookie(newAccessToken);

        Cookie newRefreshToken = new Cookie(REFRESH_TOKEN, newToken.getRefreshToken());
        newRefreshToken.setHttpOnly(true);
        newRefreshToken.setSecure(true);
        newRefreshToken.setPath("/");
        newRefreshToken.setMaxAge(10 * 24 * 60 * 60); // 10일
        newRefreshToken.setDomain(BASERURL.split("//")[1]);
        newRefreshToken.setAttribute("SameSite", "None");
        response.addCookie(newRefreshToken);

        log.info("[createBothToken] Access Token, Refresh Token 발급 완료, email = {}, Access: {}",authentication.getName(), newToken.getAccessToken());
    }

    /**
     * 로그아웃 - Refresh Token 삭제
     */
    public void deleteToken(HttpServletResponse response) {
        // 어차피 JwtAuthenticationFilter 단에서 토큰을 검증하여 인증을 처리하므로
        // SecurityContext에 Authentication 객체가 없는 경우는 없다.
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName()!=null){
            redisStorageService.deleteRefreshToken(authentication.getName());
            deleteCookie(response);
            log.info("[deleteToken] Refresh Token 삭제 완료");
        }
    }

    private void deleteCookie(HttpServletResponse response) {
        Cookie refreshCookie = new Cookie(REFRESH_TOKEN, "");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0); // 7일
        response.addCookie(refreshCookie);
    }

    public void setAuthentication(ServletServerHttpRequest serverHttpRequest){
        String accessToken = jwtUtils.getAccessToken(serverHttpRequest.getServletRequest());

        // Access Token이 있는 경우
        if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
            String email = jwtTokenProvider.getUsername(accessToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // 토큰이 유효하고, SecurityContext에 Authentication 객체가 없는 경우
            if (userDetails != null) {
                // Authentication 객체 생성 후 SecurityContext에 저장 (인증 완료)
                JwtAuthenticationToken authentication = new JwtAuthenticationToken(userDetails, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } else {
            SecurityContextHolder.clearContext();
            throw new MalformedJwtException("[Chatting] JWT를 찾을 수 없습니다.");
        }
    }
}