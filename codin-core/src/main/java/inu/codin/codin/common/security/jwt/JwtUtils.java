package inu.codin.codin.common.security.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtUtils {

    /**
     * 쿠키에서 Access 토큰 추출
     * HTTP Cookies : "Authorization" : "..."
     * @return (null, 빈 문자열)의 경우 null 반환
     */
    public String getAccessToken(HttpServletRequest request) {
//        String bearerToken = request.getHeader("Authorization");
        String bearerToken = null;
        if (request.getCookies() != null){
            for (Cookie cookie : request. getCookies()){
                if ("Authorization".equals(cookie.getName())){
                    bearerToken = cookie.getValue();
                    break;
                }
            }
        }

        if (StringUtils.hasText(bearerToken)) {
            return bearerToken;
        }
        return null;
    }

    /**
     * 쿠키에서 Refresh 토큰 추출
     * HTTP Cookies : "RefreshToken" : "..."
     * @return RefreshToken이 없는 경우 null 반환
     */
    public String getRefreshToken(HttpServletRequest request) {
        String refreshToken = null;
        if (request.getCookies() != null){
            for (Cookie cookie : request. getCookies()){
                if ("RefreshToken".equals(cookie.getName())){
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (StringUtils.hasText(refreshToken)) {
            return refreshToken;
        }
        return null;
    }

}
