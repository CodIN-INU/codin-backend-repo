package inu.codin.security.exception;

import inu.codin.common.exception.GlobalErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SecurityErrorCode implements GlobalErrorCode {

    INVALID_TOKEN( HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    TOKEN_NOT_FOUND( HttpStatus.UNAUTHORIZED, "토큰이 존재하지 않습니다."),
    INVALID_SIGNATURE( HttpStatus.UNAUTHORIZED, "잘못된 토큰 서명입니다."),
    ACCESS_DENIED( HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    ACCOUNT_LOCKED( HttpStatus.FORBIDDEN, "계정이 잠겼습니다. 관리자에게 문의하세요."),
    INVALID_CREDENTIALS( HttpStatus.UNAUTHORIZED, "잘못된 인증 정보입니다."),
    INVALID_TYPE( HttpStatus.BAD_REQUEST, "잘못된 토큰 타입입니다.");

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
