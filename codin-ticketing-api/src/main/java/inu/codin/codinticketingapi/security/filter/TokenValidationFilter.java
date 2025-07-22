package inu.codin.codinticketingapi.security.filter;

import inu.codin.codinticketingapi.security.jwt.JwtTokenValidator;
import inu.codin.codinticketingapi.security.jwt.TokenUserDetails;
import inu.codin.codinticketingapi.security.util.TokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 토큰 검증 전용 필터
 */
@RequiredArgsConstructor
@Slf4j
public class TokenValidationFilter extends OncePerRequestFilter {
    private final JwtTokenValidator jwtTokenValidator;

    private final String [] SWAGGER_AUTH_PATHS = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/v3/api-docs",
            "/swagger-resources/**"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = TokenUtil.extractToken(request);

        if (accessToken != null && jwtTokenValidator.validateAccessToken(accessToken)) {
            log.debug("[TokenValidationFilter] Access Token이 유효함");
            // Access Token이 있고 유효한 경우
            setAuthentication(accessToken);
        } else {
            log.debug("[TokenValidationFilter] Access Token이 유효하지 않음");
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 토큰에서 인증 정보 생성 후 SecurityContext에 설정
     */
    private void setAuthentication(String token) {
        try {
            String userId = jwtTokenValidator.getUserId(token);
            String username = jwtTokenValidator.getUsername(token);
            String role = jwtTokenValidator.getUserRole(token);
            log.debug("[setAuthentication] : {}, {}", userId, role);

            TokenUserDetails userDetails = TokenUserDetails.fromTokenClaims(userId, username, role, token);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("[TokenValidationFilter] Authentication 설정 완료: userId={}, username={}, role={}", userId, username, role);
        } catch (Exception e) {
            log.error("[TokenValidationFilter] 인증 설정 실패: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
    }
}
