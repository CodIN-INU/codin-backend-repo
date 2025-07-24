package inu.codin.codin.domain.like.exception;

import inu.codin.codin.global.common.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum LikeErrorCode implements GlobalErrorCode {
    LIKE_UNEXPECTED_MESSAGE(HttpStatus.INTERNAL_SERVER_ERROR, "예상치 못한 좋아요 메세지 입니다.");

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
