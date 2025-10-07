package inu.codin.codin.domain.post.domain.poll.exception;

import inu.codin.codin.common.exception.GlobalException;
import lombok.Getter;

@Getter
public class PollException extends GlobalException {
    private final PollErrorCode errorCode;
    public PollException(PollErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
} 