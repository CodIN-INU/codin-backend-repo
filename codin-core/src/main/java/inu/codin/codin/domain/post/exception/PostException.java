package inu.codin.codin.domain.post.exception;

import inu.codin.codin.common.exception.GlobalException;
import lombok.Getter;

@Getter
public class PostException extends GlobalException {
    private final PostErrorCode errorCode;
    public PostException(PostErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
