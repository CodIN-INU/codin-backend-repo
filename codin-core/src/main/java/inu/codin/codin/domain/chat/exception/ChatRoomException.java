package inu.codin.codin.domain.chat.exception;

import inu.codin.codin.common.exception.GlobalException;
import lombok.Getter;

@Getter
public class ChatRoomException extends GlobalException {

    private final ChatRoomErrorCode errorCode;
    public ChatRoomException(ChatRoomErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
