package inu.codin.codin.domain.chat.chatroom.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @Schema(description = "채팅방 _id", example = "1111111")
    private final String chatRoomId;

    @NotBlank
    @Schema(description = "채팅방 제목", example = "채팅해요")
    private final String roomName;

    @Schema(description = "가장 최근 채팅 내역 메세지", example = "안녕하세요")
    private final String message;

    @Schema(description = "가장 최근 채팅 내역 시간", example = "2024-11-29")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private final LocalDateTime currentMessageDate;

    @Schema(description = "채팅방 알림 설정", example = "true")
    private final boolean notificationEnabled;

    @Builder
    public ChatRoomListResponseDto(String chatRoomId, String roomName, String message, LocalDateTime currentMessageDate, boolean notificationEnabled) {
        this.chatRoomId = chatRoomId;
        this.roomName = roomName;
        this.message = message;
        this.currentMessageDate = currentMessageDate;
        this.notificationEnabled = notificationEnabled;
    }

    public static ChatRoomListResponseDto of(ChatRoom chatRoom, Chatting chatting) {
        return ChatRoomListResponseDto.builder()
                .chatRoomId(chatRoom.get_id().toString())
                .roomName(chatRoom.getRoomName())
                .message(chatting==null ? null : chatting.getContent())
                .currentMessageDate(chatting==null ? null : chatting.getCreatedAt())
                .build();
    }
}
