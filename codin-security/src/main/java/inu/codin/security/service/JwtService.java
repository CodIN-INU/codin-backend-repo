package inu.codin.security.service;

import inu.codin.security.exception.JwtException;
import inu.codin.security.exception.SecurityErrorCode;
import inu.codin.security.jwt.JwtAuthenticationToken;
import inu.codin.security.jwt.JwtTokenValidator;
import inu.codin.security.jwt.TokenUserDetails;
import inu.codin.security.util.TokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * JWT 토큰 검증 전용 서비스
 * 
 * 기존 JwtService에서 검증 기능만 분리
 * - 토큰 발급 기능 제거 (codin-auth로 이동 예정)
 * - 도메인 의존성 제거 (CustomUserDetailsService, RedisStorageService)
 * - 순수한 토큰 검증만 담당
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class JwtService {

    private final JwtTokenValidator jwtTokenValidator;

    /**
     * 요청에서 토큰을 추출하고 검증하여 SecurityContext에 설정
     * 
     * @param request HTTP 요청
     * @return 검증 성공 여부
     */
    public boolean validateAndSetAuthentication(HttpServletRequest request) {
        try {
            // 1. 토큰 추출
            String token = TokenUtil.extractToken(request);
            if (token == null) {
                log.debug("[validateAndSetAuthentication] 토큰이 없습니다.");
                return false;
            }

            // 2. 토큰 검증
            if (!jwtTokenValidator.validateAccessToken(token)) {
                log.debug("[validateAndSetAuthentication] 토큰 검증 실패");
                return false;
            }

            // 3. 토큰에서 사용자 정보 추출
            String userId = jwtTokenValidator.getUserId(token);
            String email = jwtTokenValidator.getUsername(token);
            String role = jwtTokenValidator.getUserRole(token);

            // 4. TokenUserDetails 생성
            TokenUserDetails userDetails = TokenUserDetails.fromTokenClaims(
                userId, email, role, token
            );

            // 5. Authentication 객체 생성 후 SecurityContext에 설정
            JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                userDetails, userDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("[validateAndSetAuthentication] 인증 성공: userId={}, email={}, role={}", 
                     userId, email, role);
            return true;

        } catch (JwtException e) {
            log.warn("[validateAndSetAuthentication] JWT 검증 실패: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("[validateAndSetAuthentication] 예상치 못한 오류: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 토큰 유효성만 검증 (SecurityContext 설정 없이)
     * 
     * @param token JWT 토큰
     * @return 유효성 검증 결과
     */
    public boolean isValidToken(String token) {
        try {
            return jwtTokenValidator.validateAccessToken(token);
        } catch (Exception e) {
            log.debug("[isValidToken] 토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 요청에서 토큰을 추출하여 유효성 검증
     * 
     * @param request HTTP 요청
     * @return 유효성 검증 결과
     */
    public boolean isValidRequest(HttpServletRequest request) {
        String token = TokenUtil.extractToken(request);
        return token != null && isValidToken(token);
    }

    //todo: 임시 메서드 생성 : pahse 2 에서 auth 에서 분리구현 예정
    public void deleteToken(HttpServletResponse response) {}
    public void checkRefreshTokenAndReissue(HttpServletRequest request, HttpServletResponse response) {}
    public void setAuthentication(HttpServletRequest servletRequest) {}
}