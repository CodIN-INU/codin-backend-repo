package inu.codin.lecture.global.exception;

import inu.codin.common.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum UserErrorCode implements GlobalErrorCode {

    FETCH_USER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error: User 정보를 가져올 수 없습니다.");

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
