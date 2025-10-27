package inu.codin.codinticketingsse.sse.exception;

import inu.codin.codinticketingsse.common.exception.GlobalException;
import lombok.Getter;

@Getter
public class SseException extends GlobalException {

    private final SseErrorCode errorCode;

    public SseException(SseErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
