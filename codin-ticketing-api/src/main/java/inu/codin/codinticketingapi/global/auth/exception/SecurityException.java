package inu.codin.codinticketingapi.global.auth.exception;

import inu.codin.codinticketingapi.global.exception.GlobalException;
import lombok.Getter;

@Getter
public class SecurityException extends GlobalException {
    private final SecurityErrorCode securityErrorCode;

    public SecurityException(SecurityErrorCode errorCode) {
        super(errorCode);
        this.securityErrorCode = errorCode;
    }
}
