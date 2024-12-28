package inu.codin.codin.domain.chat.chatting.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ChattingAndUserIdResponseDto {

    private List<ChattingResponseDto> chatting;

    private String currentUserId;

    @Builder
    public ChattingAndUserIdResponseDto(List<ChattingResponseDto> chatting, String currentUserId) {
        this.chatting = chatting;
        this.currentUserId = currentUserId;
    }
}
