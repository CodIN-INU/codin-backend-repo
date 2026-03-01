package inu.codin.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import inu.codin.common.response.ExceptionResponse;
import inu.codin.security.exception.JwtException;
import inu.codin.security.exception.SecurityErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 예외 처리 필터
 * - JwtException 발생 시, INVALID_TOKEN 응답 (401)
 * - 그 외 예외 발생 시, INTERNAL_SERVER_ERROR 응답 (500)
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ExceptionHandlerFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            log.warn("[doFilterInternal] JwtException: {}", e.getMessage());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, e.getErrorCode().getMessage());
        } catch (Exception e) {
            log.error("[doFilterInternal] Unexpected exception: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(), new ExceptionResponse(message, status.value()));
    }

}
