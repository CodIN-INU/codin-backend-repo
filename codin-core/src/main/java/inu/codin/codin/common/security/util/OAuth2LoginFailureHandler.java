package inu.codin.codin.common.security.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import inu.codin.codin.common.response.ExceptionResponse;
import inu.codin.codin.common.security.service.JwtService;
import inu.codin.codin.common.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final JwtService jwtService;

    @Value("${server.domain}")
    private String BASEURL;
    public final static String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";


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

        removeAllToken(request, response);

        getRedirectStrategy().sendRedirect(request, response, BASEURL + "/login" + (errorCode != null ? "?error=" + errorCode : ""));
    }

    private void removeAllToken(HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        jwtService.deleteToken(response);
    }
}
