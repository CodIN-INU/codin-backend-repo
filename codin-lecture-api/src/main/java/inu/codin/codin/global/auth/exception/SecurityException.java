package inu.codin.codin.global.auth.exception;

import inu.codin.codin.global.common.exception.GlobalException;
import lombok.Getter;

@Getter
public class SecurityException extends GlobalException {
    private final SecurityErrorCode securityErrorCode;

    public SecurityException(SecurityErrorCode errorCode) {
        super(errorCode);
        this.securityErrorCode = errorCode;
    }
}
