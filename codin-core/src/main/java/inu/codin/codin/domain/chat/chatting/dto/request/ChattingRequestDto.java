package inu.codin.codin.domain.chat.chatting.dto.request;

import inu.codin.codin.domain.chat.chatting.entity.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChattingRequestDto {

    @NotNull
    @Schema(description = "STOMP 프로토콜 type", example = "SEND")
    private MessageType type;

    @NotBlank
    @Schema(description = "수신자 Id", example = "111111")
    private String senderId;

    @NotBlank
    @Schema(description = "채팅 내용", example = "안녕하세요")
    private String content;
}
