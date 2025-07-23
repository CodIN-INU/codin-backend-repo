package inu.codin.codin.domain.review.exception;

import inu.codin.codin.global.common.exception.GlobalErrorCode;
import inu.codin.codin.global.common.exception.GlobalException;

public class ReviewException extends GlobalException {
    public ReviewException(GlobalErrorCode errorCode) {
        super(errorCode);
    }
}
