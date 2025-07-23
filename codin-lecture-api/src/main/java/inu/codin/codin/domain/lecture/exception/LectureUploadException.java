package inu.codin.codin.domain.lecture.exception;

import inu.codin.codin.global.common.exception.GlobalException;
import lombok.Getter;

@Getter
public class LectureUploadException extends GlobalException {
    private final LectureErrorCode errorCode;
    private Integer exitCode;
    private String errorMessage;

    public LectureUploadException(LectureErrorCode errorCode, int exitCode) {
        super(errorCode);
        this.errorCode = errorCode;
        this.exitCode = exitCode;
    }
    public LectureUploadException(LectureErrorCode errorCode, String errorMessage) {
        super(errorCode);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
