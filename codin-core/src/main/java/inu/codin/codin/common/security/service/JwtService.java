package inu.codin.codin.common.security.service;

import inu.codin.codin.common.security.exception.JwtException;
import inu.codin.codin.common.security.exception.SecurityErrorCode;
import inu.codin.codin.common.security.jwt.JwtAuthenticationToken;
import inu.codin.codin.common.security.jwt.JwtTokenProvider;
import inu.codin.codin.common.security.jwt.JwtUtils;
import inu.codin.codin.domain.user.security.CustomUserDetailsService;
import inu.codin.codin.infra.redis.RedisStorageService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    public void checkRefreshTokenAndReissue(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getRefreshToken(request);
        if (refreshToken == null) {
            log.error("[reissueToken] Refresh Token이 없습니다.");
            throw new JwtException(SecurityErrorCode.INVALID_TOKEN, "Refresh Token이 없습니다.");
        }

        String username = jwtTokenProvider.getUsername(refreshToken);
        validateRefreshTokenWithAccessToken(request, username);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // 토큰이 유효하고, SecurityContext에 Authentication 객체가 없는 경우
        if (userDetails != null) {
            // Authentication 객체 생성 후 SecurityContext에 저장 (인증 완료)
            JwtAuthenticationToken authentication = new JwtAuthenticationToken(userDetails, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        reissueToken(refreshToken, response);
    }

    //만료된 accessToken과 username을 비교
    private void validateRefreshTokenWithAccessToken(HttpServletRequest request, String username) {
        String accessToken = getAccessToken(request);
        String accessUsername;
        try {
            accessUsername = jwtTokenProvider.getUsername(accessToken);
        } catch (ExpiredJwtException e) {
            Claims expiredClaims = e.getClaims();
            accessUsername = expiredClaims.getSubject();
        }
        if (!accessUsername.equals(username)) {
            log.error("[reissueToken] Access Token의 username과 Refresh Token가 일치하지 않습니다.");
            throw new JwtException(SecurityErrorCode.INVALID_TOKEN, "Access Token의 username과 Refresh Token가 일치하지 않습니다.");
        }
    }

    /**
     * Refresh Token을 이용하여 Access Token, Refresh Token 재발급
     * @param refreshToken
     * @param response
     */
    public void reissueToken(String refreshToken, HttpServletResponse response) {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            log.error("[reissueToken] Refresh Token이 유효하지 않습니다.");
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

        Cookie newAccessToken = new Cookie("accessToken", newToken.getAccessToken());
        newAccessToken.setHttpOnly(true);
        newAccessToken.setSecure(true);
        newAccessToken.setPath("/");
        newAccessToken.setMaxAge(10 * 24 * 60 * 60); // 10일
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

    public void setAuthentication(HttpServletRequest request){
        String accessToken = getAccessToken(request);

        // Access Token이 있는 경우
        if (accessToken != null) {
            getUserDetailsAndSetAuthentication(accessToken);
        } else {
            SecurityContextHolder.clearContext();
            throw new MalformedJwtException("[Chatting] JWT를 찾을 수 없습니다.");
        }
    }

    public void getUserDetailsAndSetAuthentication(String token) {
        jwtTokenProvider.validateToken(token);
        String email = jwtTokenProvider.getUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // 토큰이 유효하고, SecurityContext에 Authentication 객체가 없는 경우
        if (userDetails != null) {
            // Authentication 객체 생성 후 SecurityContext에 저장 (인증 완료)
            JwtAuthenticationToken authentication = new JwtAuthenticationToken(userDetails, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }

    public String getAccessToken(HttpServletRequest request) {
        String accessToken = jwtUtils.getAccessToken(request);
        if (!jwtTokenProvider.validType(accessToken, "access")) {
            log.error("[getAccessToken] Access Token이 아닙니다.");
            throw new JwtException(SecurityErrorCode.INVALID_TYPE, "Access Token이 아닙니다.");
        }
        return accessToken;
    }

    public String getRefreshToken(HttpServletRequest request) {
        String refreshToken = jwtUtils.getRefreshToken(request);
        if (!jwtTokenProvider.validType(refreshToken, "refresh")) {
            log.error("[getRefreshToken] Refresh Token이 아닙니다.");
            throw new JwtException(SecurityErrorCode.INVALID_TYPE, "Refresh Token이 아닙니다.");
        }
        return refreshToken;
    }
}