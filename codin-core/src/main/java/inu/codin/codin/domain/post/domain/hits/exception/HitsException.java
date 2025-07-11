package inu.codin.codin.domain.post.domain.hits.exception;

import inu.codin.codin.common.exception.GlobalException;
import lombok.Getter;

@Getter
public class HitsException extends GlobalException {
    private final HitsErrorCode errorCode;
    public HitsException(HitsErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
} 