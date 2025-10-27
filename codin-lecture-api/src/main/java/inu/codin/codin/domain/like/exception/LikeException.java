package inu.codin.codin.domain.like.exception;

import inu.codin.codin.global.common.exception.GlobalException;
import lombok.Getter;

@Getter
public class LikeException extends GlobalException {
    private final LikeErrorCode errorCode;
    private final String errorMessage;
    public LikeException(LikeErrorCode errorCode, String errorMessage) {
        super(errorCode);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
