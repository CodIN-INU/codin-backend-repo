package inu.codin.codinticketingapi.common.security.exception;

import inu.codin.codinticketingapi.common.exception.GlobalException;
import lombok.Getter;

@Getter
public class SecurityException extends GlobalException {
    private final SecurityErrorCode securityErrorCode;

    public SecurityException(SecurityErrorCode errorCode) {
        super(errorCode);
        this.securityErrorCode = errorCode;
    }
}
