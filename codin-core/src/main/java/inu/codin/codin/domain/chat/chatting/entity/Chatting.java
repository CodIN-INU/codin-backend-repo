package inu.codin.codin.domain.chat.chatting.entity;

import inu.codin.codin.common.BaseTimeEntity;
import inu.codin.codin.domain.chat.chatting.dto.request.ChattingRequestDto;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "chatting")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Chatting extends BaseTimeEntity {

    @Id @NotBlank
    private String id;

    @NotBlank
    private String senderId;

    @NotBlank
    private String content;

    private String chatRoomId;

    @Builder
    public Chatting(String senderId, String content, String chatRoomId) {
        this.senderId = senderId;
        this.content = content;
        this.chatRoomId = chatRoomId;
    }

    public static Chatting of(String chatRoomId, ChattingRequestDto chattingRequestDto) {
        return Chatting.builder()
                .senderId(chattingRequestDto.getSenderId())
                .content(chattingRequestDto.getContent())
                .chatRoomId(chatRoomId)
                .build();
    }
}
