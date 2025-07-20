package inu.codin.codinticketingapi.global.exception;

import org.springframework.http.HttpStatus;

public interface GlobalErrorCode {
    HttpStatus httpStatus();
    String message();
}