package inu.codin.codin.domain.lecture.exception;

import inu.codin.codin.global.common.exception.GlobalException;
import lombok.Getter;

@Getter
public class LectureException extends GlobalException {
    public LectureException(LectureErrorCode errorCode) {
        super(errorCode);
    }
}
