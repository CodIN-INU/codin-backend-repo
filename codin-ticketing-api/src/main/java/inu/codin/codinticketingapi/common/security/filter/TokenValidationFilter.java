package inu.codin.codinticketingapi.common.security.filter;

import inu.codin.codinticketingapi.common.security.jwt.JwtTokenValidator;
import inu.codin.codinticketingapi.common.security.jwt.TokenUserDetails;
import inu.codin.codinticketingapi.common.security.util.TokenUtil;
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

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = TokenUtil.extractToken(request);

        if (accessToken != null && jwtTokenValidator.validateAccessToken(accessToken)) {
            log.info("[TokenValidationFilter] Access Token이 있고 유효한 경우");
            // Access Token이 있고 유효한 경우
            setAuthentication(accessToken);
        } else {
            log.info("[TokenValidationFilter] Access Token이 유효하지 않음");
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
            String role = jwtTokenValidator.getUserRole(token);
            log.info("[setAuthentication] : {}, {}", userId, role);

            TokenUserDetails userDetails = TokenUserDetails.fromTokenClaims(userId, role);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("[TokenValidationFilter] 인증 설정 완료: userId={}, role={}", userId, role);
        } catch (Exception e) {
            log.error("[TokenValidationFilter] 인증 설정 실패: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
    }
}
