package inu.codin.auth.exception;

import inu.codin.common.exception.GlobalErrorCode;
import inu.codin.common.exception.GlobalException;
import inu.codin.common.response.ExceptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

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
        GlobalErrorCode code = e.getErrorCode();
        return ResponseEntity.status(code.httpStatus())
                .body(new ExceptionResponse(code.message(), code.httpStatus().value()));
    }

    @ExceptionHandler(AuthException.class)
    protected ResponseEntity<ExceptionResponse> handleBlockException(AuthException e) {
        AuthErrorCode code = e.getErrorCode();
        return ResponseEntity.status(code.httpStatus())
                .body(new ExceptionResponse(code.message(), code.httpStatus().value()));
    }
}
