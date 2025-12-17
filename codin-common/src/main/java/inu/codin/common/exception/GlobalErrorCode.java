package inu.codin.common.exception;

import org.springframework.http.HttpStatus;

public interface GlobalErrorCode {
    HttpStatus httpStatus();
    String message();
}
