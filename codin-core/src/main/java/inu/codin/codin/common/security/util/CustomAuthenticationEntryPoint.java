package inu.codin.codin.common.security.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import inu.codin.codin.common.response.ExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException exception) throws IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String errorMessage = "AUTHENTICATION_REQUIRED.";
        if (exception instanceof OAuth2AuthenticationException oauth2Ex) {
            OAuth2Error error = oauth2Ex.getError();
            if (error != null && error.getDescription() != null) {
                errorMessage = error.getDescription();
            }
        }

        ExceptionResponse errorResponse = new ExceptionResponse(errorMessage, HttpServletResponse.SC_UNAUTHORIZED);
        String responseBody = objectMapper.writeValueAsString(errorResponse);

        log.error("[AuthenticationEntryPoint] {}", responseBody);
        response.getWriter().write(responseBody);
    }
}
