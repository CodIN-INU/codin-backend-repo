package inu.codin.codinticketingapi.common.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import inu.codin.codinticketingapi.common.auth.exception.SecurityException;
import inu.codin.codinticketingapi.common.response.ExceptionResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * SecurityFilterChain에서 발생하는 예외를 처리하는 필터
 */
@Slf4j
public class SecurityExceptionHandlerFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (SecurityException e) {
            log.error("[ExceptionHandlerFilter] SecurityException 발생: {}", e.getMessage());
            setErrorResponse(response, e);
        } catch (Exception e) {
            log.error("[ExceptionHandlerFilter] 예상치 못한 예외 발생: {}", e.getMessage(), e);
            // SecurityException이 아닌 다른 예외는 기존 처리 방식으로 전달
            throw e;
        }
    }

    /**
     * SecurityException을 ExceptionResponse로 변환하여 응답
     */
    private void setErrorResponse(HttpServletResponse response, SecurityException e) throws IOException {
        response.setStatus(e.getSecurityErrorCode().httpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        ExceptionResponse exceptionResponse = new ExceptionResponse(
                e.getSecurityErrorCode().httpStatus().value(),
                e.getSecurityErrorCode().message()
        );

        String jsonResponse = objectMapper.writeValueAsString(exceptionResponse);
        response.getWriter().write(jsonResponse);
    }
}
