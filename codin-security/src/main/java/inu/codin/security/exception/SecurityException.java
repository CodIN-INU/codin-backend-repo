package inu.codin.security.exception;

import lombok.Getter;

@Getter
public class SecurityException extends RuntimeException {

    private final SecurityErrorCode errorCode;

    public SecurityException(SecurityErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public SecurityException(SecurityErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
 