package inu.codin.codin.domain.lecture.exception;

import inu.codin.codin.global.common.exception.GlobalException;

public class LectureUploadException extends GlobalException {
    private int exitCode;
    private String errorMessage;

    public LectureUploadException(LectureErrorCode errorCode, int exitCode) {
        super(errorCode);
        this.exitCode = exitCode;
    }
    public LectureUploadException(LectureErrorCode errorCode, String errorMessage) {
        super(errorCode);
        this.errorMessage = errorMessage;
    }
}
