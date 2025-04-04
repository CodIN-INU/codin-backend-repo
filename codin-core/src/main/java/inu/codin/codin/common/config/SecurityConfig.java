package inu.codin.codin.common.config;


import inu.codin.codin.common.dto.PermitAllProperties;
import inu.codin.codin.common.security.filter.ExceptionHandlerFilter;
import inu.codin.codin.common.security.filter.JwtAuthenticationFilter;
import inu.codin.codin.common.security.jwt.JwtTokenProvider;
import inu.codin.codin.common.security.jwt.JwtUtils;
import inu.codin.codin.common.security.service.AppleOAuth2UserService;
import inu.codin.codin.common.security.service.CustomOAuth2UserService;
import inu.codin.codin.common.security.util.*;
import inu.codin.codin.common.util.CustomAuthorizationRequestResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Slf4j
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
    private final PermitAllProperties permitAllProperties;

    private final AppleOAuth2UserService appleOAuth2UserService;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final CustomOAuth2AccessTokenResponseClient customOAuth2AccessTokenResponseClient;

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Value("${server.domain}")
    private String BASEURL;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // cors 설정
                .csrf(CsrfConfigurer::disable) // csrf 비활성화
                .formLogin(FormLoginConfigurer::disable) // form login 비활성화
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 사용하지 않음
                )
                // authorizeHttpRequests 메서드를 통해 요청에 대한 권한 설정
                .authorizeHttpRequests((authorizeHttpRequests) ->
                        authorizeHttpRequests
                                .requestMatchers(permitAllProperties.getUrls().toArray(new String[0])).permitAll()
                                .requestMatchers(ADMIN_AUTH_PATHS).hasRole("ADMIN")
                                .requestMatchers(MANAGER_AUTH_PATHS).hasRole("MANAGER")
                                .requestMatchers(USER_AUTH_PATHS).hasRole("USER")
                                .anyRequest().hasRole("USER")
                )
                //Security 내부 인증 실패 처리 => access_token 없는 경우
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                )
                //oauth2 로그인 설정 추가
                .oauth2Login(oauth2 -> oauth2
                                 // Apple 클라이언트에 대해 커스텀 토큰 응답 클라이언트 적용
                                 .tokenEndpoint(token -> token
                                         .accessTokenResponseClient(customOAuth2AccessTokenResponseClient)
                                  )
                                 .authorizationEndpoint(authorization -> authorization
                                         //쿠키를 사용해 OAuth의 정보를 가져오고 저장
                                         .authorizationRequestRepository(oAuth2AuthorizationRequestBasedOnCookieRepository())
                                         .authorizationRequestResolver(new CustomAuthorizationRequestResolver(clientRegistrationRepository))
                                 )
//                                .redirectionEndpoint(redirection -> redirection
//                                        .baseUri("/callback/apple")  // apple 콜백 URI를 설정추가.
//                                 )
                                .userInfoEndpoint(userInfo -> userInfo
                                        .userService(delegatingOAuth2UserService())
                                )

                                .successHandler(oAuth2LoginSuccessHandler)
                                .failureHandler(oAuth2LoginFailureHandler)
                )
                                // Swagger 접근 시 httpBasic 인증 사용
//                .httpBasic(Customizer.withDefaults())
                // JwtAuthenticationFilter 추가
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService, jwtUtils, permitAllProperties),
                        UsernamePasswordAuthenticationFilter.class
                )
                // 예외 처리 필터 추가
                .addFilterBefore(new ExceptionHandlerFilter(), LogoutFilter.class);
        return http.build();
    }


    @Bean
    public OAuth2AuthorizationRequestBasedOnCookieRepository oAuth2AuthorizationRequestBasedOnCookieRepository() {
        return new OAuth2AuthorizationRequestBasedOnCookieRepository();
    }

    private OAuth2UserService<OAuth2UserRequest, OAuth2User> delegatingOAuth2UserService() {
        return userRequest -> {
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            log.info("OAuth2 registrationId: {}", registrationId);
            if ("apple".equals(registrationId)) {
                log.info("apple Login loadUser : {}", userRequest);
                return appleOAuth2UserService.loadUser(userRequest);
            } else if ("google".equals(registrationId)) {
                log.info("google Login loadUser");
                return customOAuth2UserService.loadUser(userRequest);
            } else {
                throw new OAuth2AuthenticationException(new OAuth2Error("unsupported_provider"),
                        "지원되지 않는 공급자입니다: " + registrationId);
            }
        };
    }


    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

    @Bean
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("ROLE_ADMIN > ROLE_MANAGER > ROLE_USER");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
        return new HttpSessionOAuth2AuthorizationRequestRepository();
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
                "http://localhost:8080",
                BASEURL,
                "https://front-end-peach-two.vercel.app",
                "https://e876-2406-5900-1080-882f-b519-f7cf-62b3-4ba4.ngrok-free.app",
                "http://e876-2406-5900-1080-882f-b519-f7cf-62b3-4ba4.ngrok-free.app",
                "https://appleid.apple.com"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Cache-Control"
        ));
        config.setExposedHeaders(List.of("Authorization"));
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }


    // User 권한 URL
    private static final String[] USER_AUTH_PATHS = {
            "/v3/api/test2",
            "/v3/api/test3",
    };

    // Admin 권한 URL
    private static final String[] ADMIN_AUTH_PATHS = {
            "/v3/api/test4",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/v3/api-docs",
            "/swagger-resources/**"
    };

    // Manager 권한 URL
    private static final String[] MANAGER_AUTH_PATHS = {
            "/v3/api/test5",
    };
}
