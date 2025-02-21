package inu.codin.codin.common.config;

import inu.codin.codin.common.security.filter.ExceptionHandlerFilter;
import inu.codin.codin.common.security.filter.JwtAuthenticationFilter;
import inu.codin.codin.common.security.jwt.JwtTokenProvider;
import inu.codin.codin.common.security.jwt.JwtUtils;
import inu.codin.codin.common.security.service.CustomOAuth2UserService;
import inu.codin.codin.common.security.util.OAuth2LoginFailureHandler;
import inu.codin.codin.common.security.util.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final JwtUtils jwtUtils;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정
                .csrf(CsrfConfigurer::disable) // CSRF 비활성화
                .formLogin(FormLoginConfigurer::disable) // Form 로그인 비활성화
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 안 함
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SWAGGER_AUTH_PATHS).hasRole("ADMIN")
                        .requestMatchers(PERMIT_ALL).permitAll()
                        .requestMatchers(ADMIN_AUTH_PATHS).hasRole("ADMIN")
                        .requestMatchers(MANAGER_AUTH_PATHS).hasRole("MANAGER")
                        .requestMatchers(USER_AUTH_PATHS).hasRole("USER")
                        .anyRequest().hasRole("USER")
                )
                .httpBasic(Customizer.withDefaults())
                // JwtAuthenticationFilter 추가
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService, jwtUtils),
                        UsernamePasswordAuthenticationFilter.class
                )
                // 예외 처리 필터 추가
                .addFilterBefore(new ExceptionHandlerFilter(), LogoutFilter.class)
                //oauth2 로그인 설정 추가
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler(oAuth2LoginFailureHandler)
                )
                // Content-Security-Policy 및 Frame-Options 설정
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin) // X-Frame-Options 비활성화
                );



        return http.build();
    }


    /**
     * AuthenticationManager 설정
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

    /**
     * 역할 계층 설정
     */
    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("ROLE_ADMIN > ROLE_MANAGER > ROLE_USER");
    }

    /**
     * 비밀번호 암호화 설정
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS 설정
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "https://www.codin.co.kr",
                "https://codin.inu.ac.kr",
                "https://front-end-peach-two.vercel.app",
                "http://localhost:8080"
        ));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
        config.setExposedHeaders(List.of("Authorization", "x-refresh-token"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * 토큰 없이 접근 가능한 URL
     */
    private static final String[] PERMIT_ALL = {
            "/auth/reissue",
            "/auth/logout",
            "/auth/signup/**",
            "/auth/google",
            "/v3/api/test1",
            "/ws-stomp/**",
            "/chats/**"
    };

    /**
     * Swagger 접근 가능한 URL
     */
    private static final String[] SWAGGER_AUTH_PATHS = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/v3/api-docs",
            "/swagger-resources/**"
    };

    /**
     * User 권한 URL
     */
    private static final String[] USER_AUTH_PATHS = {
            "/v3/api/test2",
            "/v3/api/test3"
    };

    /**
     * Admin 권한 URL
     */
    private static final String[] ADMIN_AUTH_PATHS = {
            "/v3/api/test4"
    };

    /**
     * Manager 권한 URL
     */
    private static final String[] MANAGER_AUTH_PATHS = {
            "/v3/api/test5"
    };
}
