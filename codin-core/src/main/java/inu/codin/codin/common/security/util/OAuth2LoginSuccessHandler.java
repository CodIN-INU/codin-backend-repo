package inu.codin.codin.common.security.util;

import inu.codin.codin.common.security.enums.AuthResultStatus;
import inu.codin.codin.common.security.jwt.JwtTokenProvider;
import inu.codin.codin.common.security.service.AuthService;
import inu.codin.codin.common.security.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final  AuthService authService;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        // OAuth2 회원가입/로그인 처리 후 JWT 발급
        AuthResultStatus result = authService.oauthLogin(oAuth2User, response);

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = response.getWriter();

        // 상황에 따라 서로 다른 응답 메시지를 반환
        switch (result) {
            case LOGIN_SUCCESS:
                writer.write("{\"code\":200, \"message\":\"정상 로그인 완료\"}");
                break;
            case NEW_USER_REGISTERED:
                writer.write("{\"code\":201, \"message\":\"신규 회원 등록 완료. 프로필 설정이 필요합니다.\"}");
                break;
            case PROFILE_INCOMPLETE:
                writer.write("{\"code\":200, \"message\":\"회원 프로필 설정 미완료. 프로필 설정 페이지로 이동해주세요.\"}");
                break;
            default:
                writer.write("{\"code\":500, \"message\":\"알 수 없는 오류 발생\"}");
                break;
        }
        writer.flush();

    }
}
