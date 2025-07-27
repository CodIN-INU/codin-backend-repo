package inu.codin.codin.global.auth.util;

import inu.codin.codin.global.auth.jwt.TokenUserDetails;
import inu.codin.codin.global.auth.exception.SecurityErrorCode;
import inu.codin.codin.global.auth.exception.SecurityException;
import io.jsonwebtoken.JwtException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * SecurityContext 관련 유틸리티 (토큰 검증 전용)
 */
public class SecurityUtils {

    /**
     * 현재 인증된 사용자의 이메일 ID 반환
     */
    public static String getUsername() {
        return getTokenUserDetails().getUsername();
    }

    /**
     * 현재 인증된 사용자의 권한 반환
     */
    public static String getCurrentUserRole() {
        return getTokenUserDetails().getRole();
    }

    /**
     * 현재 인증된 사용자의 유저 토큰 반환
     */
    public static String getUserToken() {
        return getTokenUserDetails().getToken();
    }

    /**
     * 현재 인증된 사용자의 유저 pk 반환
     */
    public static String getUserId() {
        return getTokenUserDetails().getUserId();
    }

    private static TokenUserDetails getTokenUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof TokenUserDetails userDetails)) {
            throw new SecurityException(SecurityErrorCode.ACCESS_DENIED);
        }
        return userDetails;
    }

    /**
     * 현재 사용자와 주어진 사용자 ID가 같은지 검증
     */
    public static void validateUser(String username) {
        String currentUsername = getUsername();
        if (!currentUsername.equals(username)) {
            throw new SecurityException(SecurityErrorCode.ACCESS_DENIED);
        }
    }

    /**
     * 현재 사용자가 인증되어 있는지 확인
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null &&
                authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof TokenUserDetails;
    }

    /**
     * 현재 사용자가 특정 권한을 가지고 있는지 확인
     */
    public static boolean hasRole(String role) {
        try {
            String currentRole = getCurrentUserRole();
            return role.equals(currentRole);
        } catch (JwtException e) {
            return false;
        }
    }
}