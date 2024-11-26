package inu.codin.codin.domain.chat.chatroom.dto;

import inu.codin.codin.domain.chat.chatroom.entity.ChatRoom;
import inu.codin.codin.domain.chat.chatting.Message;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class ChatRoomListResponseDto {

    private final String roomName;
    private final String message;
    private final LocalDateTime currentMessageDate;
    private final boolean notificationEnabled;

    @Builder
    public ChatRoomListResponseDto(String roomName, String message, LocalDateTime currentMessageDate, boolean notificationEnabled) {
        this.roomName = roomName;
        this.message = message;
        this.currentMessageDate = currentMessageDate;
        this.notificationEnabled = notificationEnabled;
    }

    public static ChatRoomListResponseDto of(ChatRoom chatRoom) {
        Message message = chatRoom.getMessages().get(chatRoom.getMessages().size()-1);
        return ChatRoomListResponseDto.builder()
                .roomName(chatRoom.getRoomName())
                .message(message.getContent())
                .currentMessageDate(message.getCreatedAt())
                .build();
    }
}
