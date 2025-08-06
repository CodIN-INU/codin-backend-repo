package inu.codin.codinticketingapi.common.exception;

import inu.codin.codinticketingapi.common.response.ExceptionResponse;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingErrorCode;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingException;
import inu.codin.codinticketingapi.domain.user.exception.UserErrorCode;
import inu.codin.codinticketingapi.domain.user.exception.UserException;
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
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ExceptionResponse(e.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(GlobalException.class)
    protected ResponseEntity<ExceptionResponse> handleGlobalException(GlobalException e) {
        GlobalErrorCode code = e.getErrorCode();
        log.warn("[GlobalException] Error Message : {}", e.getMessage());
        return ResponseEntity.status(code.httpStatus())
                .body(new ExceptionResponse(e.getMessage(), code.httpStatus().value()));
    }

    @ExceptionHandler(TicketingException.class)
    protected ResponseEntity<ExceptionResponse> handleTicketingException(TicketingException e) {
        TicketingErrorCode code = e.getErrorCode();
        log.warn("[TicketingException] Error Message : {}", e.getMessage());
        return ResponseEntity.status(code.httpStatus())
                .body(new ExceptionResponse(e.getMessage(), code.httpStatus().value()));
    }

    @ExceptionHandler(UserException.class)
    protected ResponseEntity<ExceptionResponse> handleUserException(UserException e) {
        UserErrorCode code = e.getErrorCode();
        log.warn("[UserException] Error Message : {}", e.getMessage());
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
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ExceptionResponse(e.getMessage(), HttpStatus.UNAUTHORIZED.value()));
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
