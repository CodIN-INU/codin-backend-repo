package inu.codin.codin.common.security.filter;

import inu.codin.codin.common.security.jwt.JwtAuthenticationToken;
import inu.codin.codin.common.security.service.JwtService;
import inu.codin.codin.common.security.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 토큰을 검증하여 인증하는 필터
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String accessToken = getTokenFromHeader(request);
        String refreshToken = getTokenFromCookie(request);

        // Access Token이 있는 경우
        if (accessToken != null && jwtTokenProvider.validateAccessToken(accessToken)) {
            String username = jwtTokenProvider.getUsername(accessToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 토큰이 유효하고, SecurityContext에 Authentication 객체가 없는 경우
            if (userDetails != null) {
                // Authentication 객체 생성 후 SecurityContext에 저장 (인증 완료)
                JwtAuthenticationToken authentication = new JwtAuthenticationToken(userDetails, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        // Refresh Token이 있는 경우 (Access Token 만료) Access Token, Refresh Token 재발급
        else if (refreshToken != null && jwtTokenProvider.validateRefreshToken(refreshToken)) {
            jwtService.reissueToken(refreshToken, response);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 헤더에서 Access 토큰 추출
     * HTTP Header : "Authorization" : "Bearer ..."
     * @return (null, 빈 문자열, "Bearer ")로 시작하지 않는 경우 null 반환
     */
    private String getTokenFromHeader(HttpServletRequest request) {
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
    private String getTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("RefreshToken")) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
