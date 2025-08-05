package inu.codin.codin.domain.review.exception;

import inu.codin.codin.global.common.exception.GlobalException;
import lombok.Getter;

@Getter
public class ReviewException extends GlobalException {
    private final ReviewErrorCode errorCode;

    public ReviewException(ReviewErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
