package inu.codin.codinticketingapi.config;

import inu.codin.codinticketingapi.security.exception.CustomAccessDeniedHandler;
import inu.codin.codinticketingapi.security.filter.SecurityExceptionHandlerFilter;
import inu.codin.codinticketingapi.security.filter.TokenValidationFilter;
import inu.codin.codinticketingapi.security.jwt.JwtTokenValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenValidator jwtTokenValidator;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Value("${server.domain}")
    private String BASE_DOMAIN_URL;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(CsrfConfigurer::disable)
                .formLogin(FormLoginConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Swagger 관련 경로 허용
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                        // 테스트 API 경로 - @PreAuthorize로 권한 제어
                        .requestMatchers("/v3/api/test**").permitAll()
                        // 모든 요청은 인증 필요, 단 특정 경로는 예외
                        .requestMatchers("/public/**").permitAll() // Public endpoints
                        .anyRequest().hasAnyRole("USER", "MANAGER", "ADMIN")
                )
                .addFilterBefore(
                        new TokenValidationFilter(jwtTokenValidator),
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterBefore(new SecurityExceptionHandlerFilter(), TokenValidationFilter.class)
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.accessDeniedHandler(customAccessDeniedHandler)
                )
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of("http://localhost:3000", BASE_DOMAIN_URL, "https://front-end-dun-mu.vercel.app"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
