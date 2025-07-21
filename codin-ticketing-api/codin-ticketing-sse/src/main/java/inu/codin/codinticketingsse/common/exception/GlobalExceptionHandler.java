package inu.codin.codinticketingsse.common.exception;

import inu.codin.codinticketingsse.common.response.ExceptionResponse;
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
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ExceptionResponse( e.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(GlobalException.class)
    protected ResponseEntity<ExceptionResponse> handleGlobalException(GlobalException e) {
        GlobalErrorCode code = e.getErrorCode();
        return ResponseEntity.status(code.httpStatus())
                .body(new ExceptionResponse(e.getMessage(), code.httpStatus().value()));
    }
}
