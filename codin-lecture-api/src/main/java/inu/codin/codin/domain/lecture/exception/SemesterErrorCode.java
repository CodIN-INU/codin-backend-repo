package inu.codin.codin.domain.lecture.exception;

import inu.codin.codin.global.common.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum SemesterErrorCode implements GlobalErrorCode {
    SEMESTER_NOT_FOUND(HttpStatus.NOT_FOUND, "학기 정보를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public HttpStatus httpStatus() {
        return httpStatus;
    }

    @Override
    public String message() {
        return message;
    }
}
