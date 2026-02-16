package inu.codin.auth.exception;

import inu.codin.common.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum AuthErrorCode implements GlobalErrorCode {

    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "아이디 혹은 비밀번호를 잘못 입력하였습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public HttpStatus httpStatus() { return httpStatus; }

    @Override
    public String message() { return message; }
}