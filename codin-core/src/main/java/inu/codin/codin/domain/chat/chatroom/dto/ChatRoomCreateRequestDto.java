package inu.codin.codin.domain.chat.chatroom.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRoomCreateRequestDto {

    private String roomName;
    private String receiverId; //채팅 수신자
}
