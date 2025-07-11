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
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ExceptionResponse( e.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(GlobalException.class)
    protected ResponseEntity<ExceptionResponse> handleGlobalException(GlobalException e) {
        GlobalErrorCode code = e.getErrorCode();
        return ResponseEntity.status(code.httpStatus())
                .body(new ExceptionResponse(e.getMessage(), code.httpStatus().value()));
    }

    @ExceptionHandler(TicketingException.class)
    protected ResponseEntity<ExceptionResponse> handleGlobalException(TicketingException e) {
        TicketingErrorCode code = e.getErrorCode();
        return ResponseEntity.status(code.httpStatus())
                .body(new ExceptionResponse(e.getMessage(), code.httpStatus().value()));
    }

    @ExceptionHandler(UserException.class)
    protected ResponseEntity<ExceptionResponse> handleGlobalException(UserException e) {
        UserErrorCode code = e.getErrorCode();
        return ResponseEntity.status(code.httpStatus())
                .body(new ExceptionResponse(e.getMessage(), code.httpStatus().value()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleValidationException(MethodArgumentNotValidException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ExceptionResponse> handleConstraintViolation(ConstraintViolationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }
}
