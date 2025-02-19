package inu.codin.codin.domain.chat.chatroom.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import inu.codin.codin.domain.chat.chatroom.entity.ChatRoom;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

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
    private final String lastMessage;

    @Schema(description = "가장 최근 채팅 내역 시간", example = "2024-11-29")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private final LocalDateTime currentMessageDate;

    @Schema(description = "안읽은 채팅 수", example = "99")
    private final int unread;

    @Builder
    public ChatRoomListResponseDto(String chatRoomId, String roomName, String lastMessage, LocalDateTime currentMessageDate, int unread) {
        this.chatRoomId = chatRoomId;
        this.roomName = roomName;
        this.lastMessage = lastMessage;
        this.currentMessageDate = currentMessageDate;
        this.unread = unread;
    }

    public static ChatRoomListResponseDto of(ChatRoom chatRoom, ObjectId userId) {
        return ChatRoomListResponseDto.builder()
                .chatRoomId(chatRoom.get_id().toString())
                .roomName(chatRoom.getRoomName())
                .lastMessage(chatRoom.getLastMessage()==null ? null : chatRoom.getLastMessage())
                .currentMessageDate(chatRoom.getUpdatedAt()==null ? null : chatRoom.getUpdatedAt())
                .unread(chatRoom.getParticipants().getInfo().get(userId).getUnreadMessage())
                .build();
    }
}
