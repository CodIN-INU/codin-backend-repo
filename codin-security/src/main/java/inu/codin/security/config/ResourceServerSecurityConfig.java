package inu.codin.security.config;

/*
 * SecurityConfig 리팩토링 이유:
 * 
 * 기존: Authorization Server + Resource Server 역할을 모두 담당하는 단일 설정
 * - OAuth2 로그인 처리 (Google, Apple)
 * - JWT 토큰 발급 
 * - JWT 토큰 검증
 * - 권한 체크
 * 
 * 변경: Resource Server 전용 설정으로 분리 (codin-security 모듈)
 * - JWT 토큰 검증만 담당
 * - 권한 체크 담당
 * - CORS 설정 담당
 * 
 * OAuth2 로그인 및 토큰 발급은 별도의 codin-auth 모듈로 분리 예정
 */

import inu.codin.security.filter.ExceptionHandlerFilter;
import inu.codin.security.filter.JwtAuthenticationFilter;
import inu.codin.security.handler.CustomAccessDeniedHandler;
import inu.codin.security.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class ResourceServerSecurityConfig {

    /*
     * Resource Server에 필요한 의존성만 유지
     * - JwtService: JWT 토큰 검증용
     * - PermitAllProperties: 인증 불필요 경로 설정
     * - PublicApiProperties: 공개 API 경로 설정
     * 
     * 제거된 의존성들 (codin-auth로 이동 예정):
     * - OAuth2LoginSuccessHandler, OAuth2LoginFailureHandler: OAuth2 로그인 처리
     * - CustomOAuth2UserService, AppleOAuth2UserService: OAuth2 사용자 정보 처리
     * - ClientRegistrationRepository: OAuth2 클라이언트 등록 정보
     * - CustomOAuth2AccessTokenResponseClient: OAuth2 토큰 응답 처리
     * - UserDetailsService: 사용자 상세 정보 서비스
     */
    private final JwtService jwtService;
    private final PermitAllProperties permitAllProperties;
    private final PublicApiProperties publicApiProperties;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Value("${server.domain:http://localhost:8080}")
    private String BASEURL;

    /**
     * Resource Server용 보안 필터 체인
     * 
     * 변경사항:
     * 1. OAuth2 로그인 설정 제거 - Authorization Server 역할 제거
     * 2. AuthenticationEntryPoint 제거 - 기본 401 응답 사용
     * 3. JWT 검증 필터만 유지 - Resource Server 핵심 기능
     * 4. 무상태 세션 정책 유지 - JWT 기반 인증
     */
    @Bean
    public SecurityFilterChain resourceServerFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 유지
                .csrf(CsrfConfigurer::disable) // CSRF 비활성화 - JWT 사용으로 불필요
                .formLogin(FormLoginConfigurer::disable) // 폼 로그인 비활성화 - JWT 전용
                .httpBasic(httpBasic -> httpBasic.disable()) // HTTP Basic 비활성화
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 무상태 세션 - JWT 기반
                )
                // 요청별 권한 설정 - Resource Server의 핵심 기능
                .authorizeHttpRequests((authorizeHttpRequests) ->
                        authorizeHttpRequests
                                .requestMatchers("/internal/**").permitAll() // 내부 feign 호출 : 추후 Internal Token Filter 로 develop
                                .requestMatchers(permitAllProperties.getUrls().toArray(new String[0])).permitAll()
                                .requestMatchers(publicApiProperties.getUrls().toArray(new String[0])).permitAll()
                                .requestMatchers(ADMIN_AUTH_PATHS).hasRole("ADMIN")
                                .requestMatchers(MANAGER_AUTH_PATHS).hasRole("MANAGER")
                                .requestMatchers(USER_AUTH_PATHS).hasRole("USER")
                                .anyRequest().hasRole("USER")
                )
                /*
                 * OAuth2 로그인 설정 제거됨 - Authorization Server로 이동
                 * 기존 .oauth2Login() 설정은 codin-auth 모듈에서 담당
                 */
                
                // JWT 인증 필터 추가 - Resource Server의 핵심 기능
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtService, permitAllProperties, publicApiProperties),
                        UsernamePasswordAuthenticationFilter.class
                )
                // 예외 처리 필터 추가
                .addFilterBefore(new ExceptionHandlerFilter(), LogoutFilter.class)
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.accessDeniedHandler(customAccessDeniedHandler)
                );

        return http.build();
    }

    /*
     * OAuth2 관련 Bean들 제거됨 - Authorization Server로 이동 예정
     * - OAuth2AuthorizationRequestBasedOnCookieRepository
     * - delegatingOAuth2UserService
     * - AuthenticationManager
     * - PasswordEncoder
     * - AuthorizationRequestRepository
     */

    /**
     * 역할 계층 구조 설정
     * Resource Server에서 권한 체크 시 사용
     * 
     * Spring Security 6.2.0 버전 변경사항:
     * - 이전 5.x: RoleHierarchyImpl.fromHierarchy() 사용
     * - 현재 6.2.0: fromHierarchy() deprecated, setHierarchy() 사용 권장
     */
    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy("ROLE_ADMIN > ROLE_MANAGER > ROLE_USER");
        return roleHierarchy;
    }
    /**
     * CORS 설정 - Resource Server에서 유지
     * 
     * 모든 마이크로서비스에서 공통으로 사용하는 CORS 정책
     * 프론트엔드 애플리케이션에서의 API 접근을 허용
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); // 쿠키 및 인증 정보 허용
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",    // 개발환경 프론트엔드
                "http://localhost:8080",    // 개발환경 백엔드
                BASEURL,                    // 운영환경 도메인
                "https://front-end-peach-two.vercel.app",  // Vercel 배포 환경
                "https://front-end-dun-mu.vercel.app",
                "https://appleid.apple.com"  // Apple OAuth 콜백 (나중에 codin-auth로 이동)
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of(
                "Authorization",     // JWT 토큰 헤더
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Cache-Control",
                "X-Refresh-Token"   // 리프레시 토큰 헤더
        ));
        config.setExposedHeaders(List.of("Authorization")); // 클라이언트에서 읽을 수 있는 헤더
        config.setMaxAge(3600L); // 프리플라이트 요청 캐시 시간
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }


    /*
     * Resource Server 권한 경로 설정
     * 
     * 이 설정들은 JWT 토큰의 권한 정보를 바탕으로 
     * API 엔드포인트별 접근 권한을 제어합니다.
     * 
     * 향후 각 마이크로서비스별로 분리하여 관리할 예정
     */
    
    // USER 권한으로 접근 가능한 경로
    private static final String[] USER_AUTH_PATHS = {
            "/v3/api/test2",
            "/v3/api/test3",
    };

    // ADMIN 권한이 필요한 경로
    private static final String[] ADMIN_AUTH_PATHS = {
            "/v3/api/test4",
    };

    // MANAGER 권한이 필요한 경로
    private static final String[] MANAGER_AUTH_PATHS = {
            "/v3/api/test5",
    };
}
