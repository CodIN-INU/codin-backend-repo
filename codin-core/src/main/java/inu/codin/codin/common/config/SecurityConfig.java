package inu.codin.codin.common.config;

import inu.codin.codin.common.dto.PermitAllProperties;
import inu.codin.codin.common.security.filter.ExceptionHandlerFilter;
import inu.codin.codin.common.security.filter.JwtAuthenticationFilter;
import inu.codin.codin.common.security.jwt.JwtTokenProvider;
import inu.codin.codin.common.security.jwt.JwtUtils;
import inu.codin.codin.common.security.service.AppleOAuth2UserService;
import inu.codin.codin.common.security.service.CustomOAuth2UserService;
import inu.codin.codin.common.security.util.OAuth2LoginFailureHandler;
import inu.codin.codin.common.security.util.OAuth2LoginSuccessHandler;
import inu.codin.codin.common.util.CustomAuthorizationRequestResolver;
import lombok.RequiredArgsConstructor;
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
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
    private final PermitAllProperties permitAllProperties;

    private final AppleOAuth2UserService appleOAuth2UserService;
    private final ClientRegistrationRepository clientRegistrationRepository;

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
                //oauth2 로그인 설정 추가
                .oauth2Login(oauth2 -> oauth2
                                 .authorizationEndpoint(authorization -> authorization
                                         .authorizationRequestResolver(
                                                 new CustomAuthorizationRequestResolver(clientRegistrationRepository))
                                  )
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

    private OAuth2UserService<OAuth2UserRequest, OAuth2User> delegatingOAuth2UserService() {
        return userRequest -> {
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            if ("apple".equals(registrationId)) {
                return appleOAuth2UserService.loadUser(userRequest);
            } else if ("google".equals(registrationId)) {
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
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("ROLE_ADMIN > ROLE_MANAGER > ROLE_USER");
    }

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
                "http://localhost:8080",
                BASEURL,
                "https://front-end-peach-two.vercel.app"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept"
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
    };

    // Manager 권한 URL
    private static final String[] MANAGER_AUTH_PATHS = {
            "/v3/api/test5",
    };
}
