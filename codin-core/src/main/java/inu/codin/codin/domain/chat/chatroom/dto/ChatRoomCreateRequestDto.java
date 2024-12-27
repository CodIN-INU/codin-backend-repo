package inu.codin.codin.domain.chat.chatroom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRoomCreateRequestDto {

    @NotBlank
    @Schema(description = "채팅방 제목", example = "채팅해요")
    private String roomName;

    @NotBlank
    @Schema(description = "채팅 수신자", example = "1111111")
    private String receiverId;
}
