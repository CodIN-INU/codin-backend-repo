package inu.codin.codin.domain.post.domain.comment.exception;

import inu.codin.codin.common.exception.GlobalException;
import lombok.Getter;

@Getter
public class CommentException extends GlobalException {
    private final CommentErrorCode errorCode;
    public CommentException(CommentErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
} 