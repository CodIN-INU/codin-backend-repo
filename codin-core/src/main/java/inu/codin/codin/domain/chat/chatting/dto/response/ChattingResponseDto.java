package inu.codin.codin.domain.chat.chatting.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import inu.codin.codin.domain.chat.chatting.entity.Chatting;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChattingResponseDto {

    @NotBlank
    private final String id;

    @NotBlank
    private final String senderId;

    @NotBlank
    private final String content;

    @NotBlank
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private final LocalDateTime createdAt;

    @NotBlank
    private final String chatRoomId;

    @Builder
    public ChattingResponseDto(String id, String senderId, String content, LocalDateTime createdAt, String chatRoomId) {
        this.id = id;
        this.senderId = senderId;
        this.content = content;
        this.createdAt = createdAt;
        this.chatRoomId = chatRoomId;
}

    public static ChattingResponseDto of(Chatting chatting){
        return ChattingResponseDto.builder()
                .id(chatting.get_id().toString())
                .senderId(chatting.getSenderId().toString())
                .content(chatting.getContent())
                .createdAt(chatting.getCreatedAt())
                .chatRoomId(chatting.getChatRoomId().toString())
                .build();
    }
}
