package inu.codin.codin.common.security.filter;

import inu.codin.codin.common.dto.PermitAllProperties;
import inu.codin.codin.common.security.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
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
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final String [] SWAGGER_AUTH_PATHS = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/v3/api-docs",
            "/swagger-resources/**"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        if (permitAllProperties.getUrls().stream().anyMatch(url -> pathMatcher.match(url, requestURI))) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = null;
        if (Arrays.stream(SWAGGER_AUTH_PATHS).anyMatch(url -> pathMatcher.match(url, requestURI))) {
            token = jwtService.getRefreshToken(request);
        } else {
            token = jwtService.getAccessToken(request);
        }

        // Access Token이 있는 경우
        if (token != null) {
            jwtService.getUserDetailsAndSetAuthentication(token);
        } else {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
