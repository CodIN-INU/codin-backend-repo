package inu.codin.codin.common.security.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import inu.codin.codin.common.response.ExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${server.domain}")
    private String BASEURL;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        response.getWriter().write(responseBody);
        if (errorCode == null)
            getRedirectStrategy().sendRedirect(request, response, BASEURL+"/login?");
        else
            getRedirectStrategy().sendRedirect(request, response, BASEURL+"/login?error="+errorCode);

    }
}
