package inu.codin.codin.domain.block.exception;

import inu.codin.codin.common.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum BlockErrorCode implements GlobalErrorCode {
    SELF_BLOCKED(HttpStatus.BAD_REQUEST, "자신을 차단할 수 없습니다."),
    SELF_UNBLOCKED(HttpStatus.BAD_REQUEST, "자신을 차단 해제할 수 없습니다."),
    ALREADY_BLOCKED(HttpStatus.BAD_REQUEST, "이미 차단한 사용자입니다."),
    BLOCKING_USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "차단하는 사용자를 찾을 수 없습니다."),
    BLOCKED_USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "차단할 사용자를 찾을 수 없습니다.");

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
