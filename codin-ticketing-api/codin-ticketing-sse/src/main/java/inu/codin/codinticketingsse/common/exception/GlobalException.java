package inu.codin.codinticketingsse.common.exception;

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
