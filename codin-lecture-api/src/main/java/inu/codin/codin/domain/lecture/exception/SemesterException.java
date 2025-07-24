package inu.codin.codin.domain.lecture.exception;

import inu.codin.codin.global.common.exception.GlobalException;

public class SemesterException extends GlobalException {
    private final SemesterErrorCode errorCode;

    public SemesterException(SemesterErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
