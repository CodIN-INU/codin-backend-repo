package inu.codin.codin.common.security.util;

import inu.codin.codin.common.security.enums.AuthResultStatus;
import inu.codin.codin.common.security.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
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

    private final AuthService authService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        // OAuth2 회원가입/로그인 처리 후 JWT 발급
        AuthResultStatus result = authService.oauthLogin(oAuth2User, response);

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = response.getWriter();


        // 상황에 따라 서로 다른 응답 메시지를 반환
        switch (result) {
            case LOGIN_SUCCESS -> {
                getRedirectStrategy().sendRedirect(request, response, BASEURL + "/main");
                log.info("{\"code\":200, \"message\":\"정상 로그인 완료\"}");
            }
            case NEW_USER_REGISTERED -> {
                getRedirectStrategy().sendRedirect(request, response, BASEURL + "/auth/profile?email=" + oAuth2User.getAttribute("email"));
                log.info("{\"code\":201, \"message\":\"신규 회원 등록 완료. 프로필 설정이 필요합니다.\"}");
            }
            case PROFILE_INCOMPLETE -> {
                getRedirectStrategy().sendRedirect(request, response, BASEURL + "/auth/profile?email=" + oAuth2User.getAttribute("email"));
                log.info("{\"code\":200, \"message\":\"회원 프로필 설정 미완료. 프로필 설정 페이지로 이동해주세요.\"}");
            }
            case SUSPENDED_USER -> {
                //todo MVC 호출을 위해 api가 붙음, 이후 삭제 예정
                getRedirectStrategy().sendRedirect(request, response, BASEURL+ "/api/suspends?endDate=" + authService.getSuspensionEndDate(oAuth2User));
                log.info("{\"code\":200, \"message\":\"정지된 회원에 대하여 정지 화면 호출\"}");
            }
            default -> {
                getRedirectStrategy().sendRedirect(request, response, BASEURL + "/login");
                log.info("{\"code\":500, \"message\":\"알 수 없는 오류 발생\"}");
            }
        }
        writer.flush();

    }
}
