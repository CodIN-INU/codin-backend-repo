package inu.codin.codin.domain.lecture.exception;

import inu.codin.codin.global.common.exception.GlobalErrorCode;
import inu.codin.codin.global.common.exception.GlobalException;

public class SemesterException extends GlobalException {
    public SemesterException(GlobalErrorCode errorCode) {
        super(errorCode);
    }
}
