package inu.codin.codin.common.exception;

import inu.codin.codin.common.response.ExceptionResponse;
import inu.codin.codin.common.security.exception.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ExceptionResponse> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ExceptionResponse(e.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(NotFoundException.class)
    protected ResponseEntity<ExceptionResponse> handleNotFoundException(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ExceptionResponse(e.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(JwtException.class)
    protected ResponseEntity<ExceptionResponse> handleJwtException(JwtException e) {
        if (e.getErrorCode().getErrorCode().equals("SEC_005")){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ExceptionResponse(e.getMessage(), HttpStatus.FORBIDDEN.value()));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ExceptionResponse(e.getMessage(), HttpStatus.UNAUTHORIZED.value()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleValidationExceptions(MethodArgumentNotValidException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ExceptionResponse(e.getBindingResult().getFieldErrors().get(0).getDefaultMessage(), HttpStatus.UNAUTHORIZED.value()));    }

}
