package inu.codin.security.filter;

import inu.codin.security.config.PermitAllProperties;
import inu.codin.security.config.PublicApiProperties;
import inu.codin.security.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

/**
 * JWT 토큰을 검증하여 인증하는 필터
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final PermitAllProperties permitAllProperties;
    private final PublicApiProperties publicApiProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final String [] SWAGGER_AUTH_PATHS = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/v3/api-docs",
            "/swagger-resources/**"
    };

    /**
     * JWT 토큰 검증 및 인증 처리
     * 
     * 변경사항:
     * - JwtService를 사용하여 토큰 검증 간소화
     * - Swagger 관련 특별 처리 제거 (일관된 JWT 검증)
     * - 의존성 최소화
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // 1. 인증이 필요하지 않은 경로 체크
        final boolean isPermitAll = permitAllProperties.getUrls().stream()
                .anyMatch(url -> pathMatcher.match(url, requestURI)) ||
                Arrays.stream(SWAGGER_AUTH_PATHS).anyMatch(url -> pathMatcher.match(url, requestURI));

        final boolean isPublicApi = publicApiProperties.getUrls().stream()
                .anyMatch(url -> pathMatcher.match(url, requestURI));

        if (isPermitAll) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. JWT 토큰 검증 및 인증 설정
        boolean isAuthenticated = jwtService.validateAndSetAuthentication(request);
        
        // 3. 인증 실패 시 PublicApi 경로는 통과, 나머지는 차단
        if (!isAuthenticated) {
            SecurityContextHolder.clearContext();
            
            if (isPublicApi) {
                filterChain.doFilter(request, response);
                return;
            }
            // 인증이 필요한 경로에서 토큰이 없거나 유효하지 않으면 401 에러 발생
            // (ExceptionHandlerFilter에서 처리됨)
        }

        filterChain.doFilter(request, response);
    }
}
