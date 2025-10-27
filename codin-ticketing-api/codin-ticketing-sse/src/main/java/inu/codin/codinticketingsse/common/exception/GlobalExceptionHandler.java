package inu.codin.codinticketingsse.common.exception;

import inu.codin.codinticketingsse.common.response.ExceptionResponse;
import inu.codin.codinticketingsse.security.exception.SecurityErrorCode;
import inu.codin.codinticketingsse.security.exception.SecurityException;
import inu.codin.codinticketingsse.sse.exception.SseErrorCode;
import inu.codin.codinticketingsse.sse.exception.SseException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ExceptionResponse> handleException(Exception e) {
        log.warn("[Exception] Class: {}, Error Message : {}, Stack Trace: {}",
                e.getClass().getSimpleName(),
                e.getMessage(),
                e.getStackTrace()[0].toString());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }

    @ExceptionHandler(GlobalException.class)
    protected ResponseEntity<ExceptionResponse> handleGlobalException(GlobalException e) {
        log.warn("[GlobalException] Class: {}, Error Message : {}", e.getClass().getSimpleName(), e.getMessage());
        GlobalErrorCode code = e.getErrorCode();
        return ResponseEntity.status(code.httpStatus())
                .body(new ExceptionResponse(e.getMessage(), code.httpStatus().value()));
    }

    @ExceptionHandler(SseException.class)
    public ResponseEntity<ExceptionResponse> handleSseException(SseException e) {
        log.warn("[SseException] Class: {}, Error Message : {}", e.getClass().getSimpleName(), e.getMessage());
        SseErrorCode code = e.getErrorCode();
        return ResponseEntity.status(code.httpStatus())
                .body(new ExceptionResponse(e.getMessage(), code.httpStatus().value()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ExceptionResponse> handleSecurityException(SecurityException e) {
        log.warn("[SecurityException] Class: {}, Error Message : {}",  e.getClass().getSimpleName(), e.getMessage());
        SecurityErrorCode code = e.getSecurityErrorCode();
        return ResponseEntity.status(code.httpStatus())
                .body(new ExceptionResponse(e.getMessage(), code.httpStatus().value()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("[MethodArgumentNotValidException] Error Message : {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ExceptionResponse> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("[ConstraintViolationException] Error Message : {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionResponse> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("[AccessDeniedException] Error Message : {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ExceptionResponse(e.getMessage(), HttpStatus.FORBIDDEN.value()));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ExceptionResponse> handleMissingServletRequestPartException(MissingServletRequestPartException e) {
        log.warn("[MissingServletRequestPartException] Error Message : {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("[MethodArgumentTypeMismatchException] Error Message : {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(ConversionFailedException.class)
    public ResponseEntity<ExceptionResponse> handleConversionFailedException(ConversionFailedException e) {
        log.warn("[ConversionFailedException] Error Message : {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }
}
