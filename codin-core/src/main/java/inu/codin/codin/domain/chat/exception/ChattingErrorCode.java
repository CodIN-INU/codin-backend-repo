package inu.codin.codin.domain.chat.exception;

import inu.codin.codin.common.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ChattingErrorCode implements GlobalErrorCode {

    CHATTING_USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "헤더에서 유저(email)을 찾을 수 없습니다."),
    CHATTING_ID_NOT_FOUND(HttpStatus.NOT_FOUND, "헤더에서 채팅방 _id를 찾을 수 없습니다.");

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
