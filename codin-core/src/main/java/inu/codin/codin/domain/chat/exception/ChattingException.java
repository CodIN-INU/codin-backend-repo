package inu.codin.codin.domain.chat.exception;

import inu.codin.codin.common.exception.GlobalException;
import lombok.Getter;

@Getter
public class ChattingException extends GlobalException {

    private final ChattingErrorCode errorCode;
    private final String sessionId;
    public ChattingException(ChattingErrorCode errorCode, String sessionId) {
        super(errorCode);
        this.errorCode = errorCode;
        this.sessionId = sessionId;
    }
}
