package inu.codin.codinticketingsse.common.exception;

import lombok.Getter;

public class GlobalException extends RuntimeException{

    private final GlobalErrorCode errorCode;

    public GlobalException(GlobalErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    public GlobalErrorCode getErrorCode() {
        return errorCode;
    }
}
