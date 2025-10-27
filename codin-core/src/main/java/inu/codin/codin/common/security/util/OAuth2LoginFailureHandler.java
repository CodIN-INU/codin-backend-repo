package inu.codin.codin.common.security.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import inu.codin.codin.common.response.ExceptionResponse;
import inu.codin.codin.common.security.service.JwtService;
import inu.codin.codin.common.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final JwtService jwtService;
    private final HttpSession httpSession;

    @Value("${server.domain}")
    private String BASEURL;
    public final static String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    private final String OAUTH2_ACCESS_TOKEN = "oauth2_access_token";


    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String errorMessage = "인증 실패.";
        String errorCode = null;
        if (exception instanceof OAuth2AuthenticationException oauth2Ex) {
            OAuth2Error error = oauth2Ex.getError();
            if (error != null && error.getDescription() != null) {
                errorMessage = error.getDescription();
                errorCode = error.getErrorCode();
            }
        }

        ExceptionResponse errorResponse = new ExceptionResponse(errorMessage, HttpServletResponse.SC_UNAUTHORIZED);
        String responseBody = objectMapper.writeValueAsString(errorResponse);

        log.error("[OAuth2LoginFailureHandler] {}", responseBody);

        // 🔹 Access Token 가져오기 (요청 파라미터에서 추출)
        String accessToken = (String) httpSession.getAttribute(OAUTH2_ACCESS_TOKEN);

        if (accessToken != null && !accessToken.isEmpty()) {
            String revokeUrl = "https://accounts.google.com/o/oauth2/revoke?token=" + accessToken;
            try {
                restTemplate.getForObject(revokeUrl, String.class);
                log.info("[OAuth2LoginFailureHandler] Google Access Token revoked successfully.");
            } catch (Exception e) {
                log.error("[OAuth2LoginFailureHandler] Failed to revoke Google Access Token: {}", e.getMessage());
            }
        } else {
            log.warn("[OAuth2LoginFailureHandler] No access token found in request.");
        }

        removeAllToken(request, response);

        if (errorCode == null)
            getRedirectStrategy().sendRedirect(request, response, BASEURL+"/login");
        else {
            getRedirectStrategy().sendRedirect(request, response, BASEURL + "/login?error=" + errorCode);
        }
    }

    private void removeAllToken(HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        jwtService.deleteToken(response);
    }
}
