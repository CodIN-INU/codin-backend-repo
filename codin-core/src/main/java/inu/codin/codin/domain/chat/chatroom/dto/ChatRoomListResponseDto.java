package inu.codin.codin.domain.chat.chatroom.dto;

import inu.codin.codin.domain.chat.chatroom.entity.ChatRoom;
import inu.codin.codin.domain.chat.chatting.entity.Chatting;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChatRoomListResponseDto {

    @NotBlank
    @Schema(description = "채팅방 제목", example = "채팅해요")
    private final String roomName;

    @Schema(description = "가장 최근 채팅 내역 메세지", example = "안녕하세요")
    private final String message;

    @Schema(description = "가장 최근 채팅 내역 시간", example = "2024-11-29")
    private final LocalDateTime currentMessageDate;

    @Schema(description = "채팅방 알림 설정", example = "true")
    private final boolean notificationEnabled;

    @Builder
    public ChatRoomListResponseDto(String roomName, String message, LocalDateTime currentMessageDate, boolean notificationEnabled) {
        this.roomName = roomName;
        this.message = message;
        this.currentMessageDate = currentMessageDate;
        this.notificationEnabled = notificationEnabled;
    }

    public static ChatRoomListResponseDto of(ChatRoom chatRoom, Chatting chatting) {
        return ChatRoomListResponseDto.builder()
                .roomName(chatRoom.getRoomName())
                .message(chatting.getContent())
                .currentMessageDate(chatting.getCreatedAt())
                .build();
    }
}
