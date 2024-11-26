package inu.codin.codin.domain.chat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRequestDto {
    private Long senderId;
    private String content;
}
