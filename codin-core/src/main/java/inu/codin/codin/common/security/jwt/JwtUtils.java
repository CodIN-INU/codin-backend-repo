package inu.codin.codin.common.security.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtUtils {

    private final String REFRESH_TOKEN = "x-refresh-token";
    private final String ACCESS_TOKEN = "Authorization";

    /**
     * 헤더에서 Access 토큰 추출
     * HTTP Header : "Authorization" : "Bearer ..."
     *
     * + 쿠키에서 Authorization 쿠키에서 추출
     * @return (null, 빈 문자열)의 경우 null 반환
     */
    public String getAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(ACCESS_TOKEN);
        if (StringUtils.hasText(bearerToken)) {
            return bearerToken.substring(7);
        }

        // todo: 쿠키 추출 방식 추후 제거
        if (request.getCookies() != null){
            for (Cookie cookie : request. getCookies()){
                if ("x-access-token".equals(cookie.getName())){
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
     * HTTP Cookies : "x-refresh-token" : "..."
     * @return RefreshToken이 없는 경우 null 반환
     */
    public String getRefreshToken(HttpServletRequest request) {
        String refreshToken = null;
        if (request.getCookies() != null){
            for (Cookie cookie : request. getCookies()){
                if (REFRESH_TOKEN.equals(cookie.getName())){
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
