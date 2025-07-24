package inu.codin.codinticketingapi.common.exception;

import inu.codin.codinticketingapi.common.response.ExceptionResponse;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingErrorCode;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingException;
import inu.codin.codinticketingapi.domain.user.exception.UserErrorCode;
import inu.codin.codinticketingapi.domain.user.exception.UserException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ExceptionResponse> handleException(Exception e) {
        log.warn("[Exception] Error Message : {}", e.getMessage());
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
}
