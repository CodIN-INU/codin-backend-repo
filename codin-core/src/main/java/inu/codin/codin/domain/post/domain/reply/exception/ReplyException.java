package inu.codin.codin.domain.post.domain.reply.exception;

import inu.codin.codin.common.exception.GlobalException;
import lombok.Getter;

@Getter
public class ReplyException extends GlobalException {
    private final ReplyErrorCode errorCode;
    public ReplyException(ReplyErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
} 