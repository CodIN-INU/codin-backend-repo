package inu.codin.codin.common.security.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import inu.codin.codin.common.security.enums.AuthResultStatus;
import inu.codin.codin.common.security.service.AppleAuthService;
import inu.codin.codin.common.security.service.GoogleAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${server.domain}")
    private String BASEURL;

    private final AppleAuthService appleAuthService;
    private final GoogleAuthService googleAuthService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        // 이미 인증된 사용자의 정보를 담고 있는 OAuth2User 객체 획득
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        // 인증 토큰에서 등록 ID(공급자)를 추출 [googel , apple]
        String provider = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();

        // OAuth2 회원가입/로그인 처리 후 JWT 발급
        AuthResultStatus result;
        if ("apple".equalsIgnoreCase(provider)) {
            result = appleAuthService.oauthLogin(oAuth2User, response);
        } else if ("google".equalsIgnoreCase(provider)) {
            result = googleAuthService.oauthLogin(oAuth2User, response);
        } else {
            OAuth2Error error = new OAuth2Error("unsupported_provider", "지원되지 않는 공급자입니다.", null);
            throw new OAuth2AuthenticationException(error, "지원되지 않는 공급자: " + provider);
        }
        handleLoginResult(request, response, result, oAuth2User.getAttribute("email"));
    }

    private void handleLoginResult(HttpServletRequest request, HttpServletResponse response, AuthResultStatus result, String email) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = response.getWriter();

        switch (result) {
            case LOGIN_SUCCESS -> {
                getRedirectStrategy().sendRedirect(request, response, BASEURL + "/main");
                log.info("{\"code\":200, \"message\":\"정상 로그인 완료: {}\"}", email);
            }
            case NEW_USER_REGISTERED -> {
                getRedirectStrategy().sendRedirect(request, response, BASEURL + "/auth/profile?email=" + email);
                log.info("{\"code\":201, \"message\":\"신규 회원 등록 완료: {}\"}", email);
            }
            case PROFILE_INCOMPLETE -> {
                getRedirectStrategy().sendRedirect(request, response, BASEURL + "/auth/profile?email=" + email);
                log.info("{\"code\":200, \"message\":\"회원 프로필 설정 미완료: {}\"}", email);
            }
            case SUSPENDED_USER -> {
                getRedirectStrategy().sendRedirect(request, response, BASEURL + "/api/suspends");
                log.info("{\"code\":200, \"message\":\"정지된 회원: {}\"}", email);
            }
            default -> {
                getRedirectStrategy().sendRedirect(request, response, BASEURL + "/login");
                log.info("{\"code\":500, \"message\":\"알 수 없는 오류 발생: {}\"}", email);
            }
        }
        writer.flush();
    }
}
