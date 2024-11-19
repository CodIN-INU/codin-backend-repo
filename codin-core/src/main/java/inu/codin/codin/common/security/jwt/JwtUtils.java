package inu.codin.codin.common.security.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtUtils {

    /**
     * 헤더에서 Access 토큰 추출
     * HTTP Header : "Authorization" : "Bearer ..."
     * @return (null, 빈 문자열, "Bearer ")로 시작하지 않는 경우 null 반환
     */
    public String getTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 쿠키에서 Refresh 토큰 추출
     * @return 쿠키에 RefreshToken이 없는 경우 null 반환
     */
    public String getTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("RT")) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

}
