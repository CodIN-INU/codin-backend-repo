package inu.codin.codin.common.security.filter;

import inu.codin.codin.common.security.exception.SecurityErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 예외 처리 필터
 * - 예외 발생 시, 클라이언트에게 응답을 보내는 필터
 * - JwtException 발생 시, INVALID_TOKEN 응답
 * - 그 외 예외 발생 시, INVALID_TOKEN 응답
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ExceptionHandlerFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("[doFilterInternal] Exception in ExceptionHandlerFilter: ", e);
            sendErrorResponse(response, SecurityErrorCode.INVALID_TOKEN);
        }
    }

    private void sendErrorResponse(HttpServletResponse response, SecurityErrorCode errorCode) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(errorCode.name())
                .code(errorCode.getErrorCode())
                .message(errorCode.getMessage())
                .build();

        try {
            response.getWriter().write(errorResponse.toString());
        } catch (IOException e) {
            log.error("Error writing error response", e);
        }
    }

    @Getter
    public static class ErrorResponse {
        private int status;
        private String error;
        private String code;
        private String message;
        private LocalDateTime timestamp;

        @Builder
        public ErrorResponse(int status, String error, String code, String message) {
            this.status = status;
            this.error = error;
            this.code = code;
            this.message = message;
            this.timestamp = LocalDateTime.now();
        }
    }

}
