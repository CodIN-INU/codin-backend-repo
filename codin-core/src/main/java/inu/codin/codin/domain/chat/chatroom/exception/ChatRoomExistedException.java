package inu.codin.codin.domain.chat.chatroom.exception;

import lombok.Getter;
import org.bson.types.ObjectId;

@Getter
public class ChatRoomExistedException extends RuntimeException{

    private int errorCode;
    private ObjectId chatRoomId;

    public ChatRoomExistedException(String msg, int errorCode, ObjectId chatRoomId){
        super(msg);
        this.errorCode = errorCode;
        this.chatRoomId = chatRoomId;
    }
}
