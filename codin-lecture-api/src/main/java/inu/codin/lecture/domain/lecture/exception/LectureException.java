package inu.codin.lecture.domain.lecture.exception;

import inu.codin.common.exception.GlobalException;
import lombok.Getter;

@Getter
public class LectureException extends GlobalException {

    private final LectureErrorCode errorCode;
    public LectureException(LectureErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
