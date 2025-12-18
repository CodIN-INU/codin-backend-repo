package inu.codin.auth.config;

/*
 * IMPORTANT: 이 SecurityConfig는 임시로 주석 처리됨
 * 
 * Phase 1에서 codin-security 모듈로 Resource Server 기능이 분리됨
 * Phase 2에서 codin-auth 모듈로 Authorization Server 기능을 분리할 예정
 * 
 * 현재 상태: 
 * - JWT 검증 기능 -> codin-security 모듈로 이동 완료
 * - OAuth2 로그인 기능 -> codin-core에 잠시 남아있음 (Phase 2에서 codin-auth로 이동 예정)
 * 
 * 임시 조치: 전체 설정을 주석 처리하여 컴파일 에러 방지
 * codin-core는 이제 codin-security 모듈에 의존하여 JWT 검증 기능 사용
 */

// TODO: Phase 2에서 codin-auth로 Authorization Server 기능 이동

/*
 * 기존 SecurityConfig의 역할:
 * 1. Authorization Server - OAuth2 로그인 처리 (Google, Apple)
 * 2. Resource Server - JWT 토큰 검증 및 권한 체크
 * 
 * 분리 후:
 * 1. Authorization Server -> codin-auth 모듈 (Phase 2에서 구현)
 * 2. Resource Server -> codin-security 모듈 (Phase 1에서 완료)
 */

import inu.codin.auth.service.oauth2.AppleOAuth2UserService;
import inu.codin.auth.service.oauth2.CustomOAuth2UserService;
import inu.codin.auth.util.CustomAuthorizationRequestResolver;
import inu.codin.auth.util.CustomOAuth2AccessTokenResponseClient;
import inu.codin.auth.util.OAuth2AuthorizationRequestBasedOnCookieRepository;
import inu.codin.auth.handler.OAuth2LoginFailureHandler;
import inu.codin.auth.handler.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final AppleOAuth2UserService appleOAuth2UserService;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final CustomOAuth2AccessTokenResponseClient customOAuth2AccessTokenResponseClient;

    @Bean
    @org.springframework.core.annotation.Order(1)
    public SecurityFilterChain oauth2Chain(HttpSecurity http) throws Exception {
        http
                // 이 체인은 OAuth2 엔드포인트에만 적용
                .securityMatcher("/oauth2/**", "/login/oauth2/**")

                .csrf(CsrfConfigurer::disable)
                .formLogin(FormLoginConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )

                .oauth2Login(oauth2 -> oauth2
                        .tokenEndpoint(token -> token.accessTokenResponseClient(customOAuth2AccessTokenResponseClient))
                        .authorizationEndpoint(authorization -> authorization
                                .authorizationRequestRepository(oAuth2AuthorizationRequestBasedOnCookieRepository())
                                .authorizationRequestResolver(new CustomAuthorizationRequestResolver(clientRegistrationRepository))
                        )
                        .userInfoEndpoint(userInfo -> userInfo.userService(delegatingOAuth2UserService()))
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler(oAuth2LoginFailureHandler)
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public OAuth2AuthorizationRequestBasedOnCookieRepository oAuth2AuthorizationRequestBasedOnCookieRepository() {
        return new OAuth2AuthorizationRequestBasedOnCookieRepository();
    }

    private OAuth2UserService<OAuth2UserRequest, OAuth2User> delegatingOAuth2UserService() {
        return userRequest -> {
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            if ("apple".equals(registrationId)) return appleOAuth2UserService.loadUser(userRequest);
            if ("google".equals(registrationId)) return customOAuth2UserService.loadUser(userRequest);
            throw new OAuth2AuthenticationException(new OAuth2Error("unsupported_provider"),
                    "지원되지 않는 공급자입니다: " + registrationId);
        };
    }
}