package inu.codin.codin.domain.chat.exception;

import inu.codin.codin.common.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ChatRoomErrorCode implements GlobalErrorCode {
    CHATROOM_CREATE_MYSELF(HttpStatus.BAD_REQUEST, "자기 자신과는 채팅방을 생성할 수 없습니다."),
    CHATROOM_EXISTED(HttpStatus.valueOf(303), "해당 reference에서 시작된 채팅방이 존재합니다."),
    CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."),
    PARTICIPANTS_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅방 내의 참여자를 찾을 수 없습니다.");

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
