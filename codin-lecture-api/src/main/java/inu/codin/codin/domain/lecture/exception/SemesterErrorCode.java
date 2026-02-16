package inu.codin.codin.domain.lecture.exception;

import inu.codin.common.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum SemesterErrorCode implements GlobalErrorCode {

    SEMESTER_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "올바르지 않은 형식입니다. '년도'-'분기'(24-1) 형식으로 보내주세요.", Level.WARN),
    SEMESTER_NOT_FOUND(HttpStatus.NOT_FOUND, "학기 정보를 찾을 수 없습니다.", Level.WARN);

    private final HttpStatus httpStatus;
    private final String message;
    private final Level logLevel;


    @Override
    public HttpStatus httpStatus() {
        return httpStatus;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public Level logEvent() {
        return logLevel;
    }
}
