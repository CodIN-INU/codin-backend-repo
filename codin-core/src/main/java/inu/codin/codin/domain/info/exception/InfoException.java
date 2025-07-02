package inu.codin.codin.domain.info.exception;

import inu.codin.codin.common.exception.GlobalException;
import lombok.Getter;

@Getter
public class InfoException extends GlobalException {

    private final InfoErrorCode errorCode;
    public InfoException(InfoErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
