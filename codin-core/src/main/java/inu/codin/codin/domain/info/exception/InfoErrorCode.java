package inu.codin.codin.domain.info.exception;

import inu.codin.codin.common.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum InfoErrorCode implements GlobalErrorCode {

    LAB_NOT_FOUND(HttpStatus.NOT_FOUND, "LAB 정보를 찾을 수 없습니다."),

    PROFESSOR_NOT_FOUND(HttpStatus.NOT_FOUND, "PROFESSOR 정보를 찾을 수 없습니다."),
    PROFESSOR_DUPLICATED(HttpStatus.CONFLICT, "이미 존재하는 PROFESSOR 정보 입니다."),

    PARTNER_NOT_FOUND(HttpStatus.NOT_FOUND, "PARTNER 정보를 찾을 수 없습니다.");

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
