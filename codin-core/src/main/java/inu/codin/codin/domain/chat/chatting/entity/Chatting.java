package inu.codin.codin.domain.chat.chatting.entity;

import inu.codin.codin.common.BaseTimeEntity;
import inu.codin.codin.domain.chat.chatting.dto.request.ChattingRequestDto;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "chatting")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Chatting extends BaseTimeEntity {

    @Id @NotBlank
    private ObjectId _id;

    @NotBlank
    private ObjectId senderId;

    @NotBlank
    private String content;

    private ObjectId chatRoomId;

    @Builder
    public Chatting(ObjectId senderId, String content, ObjectId chatRoomId) {
        this.senderId = senderId;
        this.content = content;
        this.chatRoomId = chatRoomId;
    }

    public static Chatting of(ObjectId chatRoomId, ChattingRequestDto chattingRequestDto, ObjectId senderId) {
        return Chatting.builder()
                .senderId(senderId)
                .content(chattingRequestDto.getContent())
                .chatRoomId(chatRoomId)
                .build();
    }
}
