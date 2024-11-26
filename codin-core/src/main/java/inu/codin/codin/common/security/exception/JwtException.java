package inu.codin.codin.common.security.exception;

import lombok.Getter;

@Getter
public class JwtException extends RuntimeException {

    private final SecurityErrorCode errorCode;

    public JwtException(SecurityErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public JwtException(SecurityErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
