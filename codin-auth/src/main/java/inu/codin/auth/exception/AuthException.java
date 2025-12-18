package inu.codin.auth.exception;

import inu.codin.common.exception.GlobalException;
import lombok.Getter;

@Getter
public class AuthException extends GlobalException {

    private final AuthErrorCode errorCode;

    public AuthException(AuthErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
