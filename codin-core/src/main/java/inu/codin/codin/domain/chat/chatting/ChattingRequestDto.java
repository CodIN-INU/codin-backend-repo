package inu.codin.codin.domain.chat.chatting;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChattingRequestDto {
    private Long senderId;
    private String content;
}
