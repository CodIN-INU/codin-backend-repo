package inu.codin.security.util;

import inu.codin.security.exception.JwtException;
import inu.codin.security.exception.SecurityErrorCode;
import inu.codin.security.jwt.TokenUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * SecurityContext와 관련된 유틸리티 클래스.
 * 
 * 변경사항:
 * - CustomUserDetails -> TokenUserDetails로 변경 (codin-security 독립성)
 * - ObjectId -> String으로 변경 (MongoDB 의존성 제거)
 * - UserRole enum 제거 -> String 사용
 * - 도메인 특화 검증 로직은 각 서비스에서 구현하도록 변경
 */
@Slf4j
public class SecurityUtils {

    /**
     * 현재 인증된 사용자의 ID를 반환.
     *
     * @return 인증된 사용자의 ID (String)
     * @throws JwtException 인증 정보가 없는 경우 예외 발생
     */
    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = getCurrentUserDetails();
        
        if (userDetails instanceof TokenUserDetails tokenUserDetails) {
            return tokenUserDetails.getUserId();
        }
        
        // 호환성을 위해 username 반환 (다른 UserDetails 구현체)
        return userDetails.getUsername();
    }

    /**
     * 현재 인증된 사용자의 ID를 반환 (nullable 안전 버전)
     * - 인증이 없거나 익명이면 null 반환
     */
    public static String getCurrentUserIdOrNull() {
        try {
            return getCurrentUserId();
        } catch (JwtException e) {
            return null;
        }
    }

    /**
     * 현재 인증된 사용자의 역할을 반환
     */
    public static String getCurrentUserRole() {
        UserDetails userDetails = getCurrentUserDetails();
        
        if (userDetails instanceof TokenUserDetails tokenUserDetails) {
            return tokenUserDetails.getRole();
        }
        
        // 기본적으로 첫 번째 권한 반환
        return userDetails.getAuthorities().iterator().next().getAuthority();
    }

    /**
     * 현재 인증된 사용자인지 검증
     */
    public static void validateUser(String userId) {
        String currentUserId = getCurrentUserId();
        if (!userId.equals(currentUserId)) {
            throw new JwtException(SecurityErrorCode.ACCESS_DENIED, "현재 유저에게 권한이 없습니다.");
        }
    }

    /**
     * 현재 인증된 사용자가 리소스 소유자인지 검증
     */
    public static void validateOwners(String currentUserId, String ownerId) {
        validateUser(currentUserId);
        if (!ownerId.equals(currentUserId)) {
            throw new JwtException(SecurityErrorCode.ACCESS_DENIED, "본인 리소스가 아닙니다.");
        }
    }
    
    /**
     * 현재 UserDetails 반환 (내부 유틸리티)
     */
    private static UserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() 
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new JwtException(SecurityErrorCode.ACCESS_DENIED);
        }
        
        if (!(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            throw new JwtException(SecurityErrorCode.ACCESS_DENIED);
        }
        
        return userDetails;
    }
}
