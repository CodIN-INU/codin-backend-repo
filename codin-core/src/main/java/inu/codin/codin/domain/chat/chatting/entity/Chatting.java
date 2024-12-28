package inu.codin.codin.domain.chat.chatting.entity;

import inu.codin.codin.common.BaseTimeEntity;
import inu.codin.codin.domain.chat.chatting.dto.ContentType;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

    private ContentType type;

    @Builder
    public Chatting(ObjectId senderId, String content, ObjectId chatRoomId, ContentType type) {
        this.senderId = senderId;
        this.content = content;
        this.chatRoomId = chatRoomId;
        this.type = type;
    }

    public static Chatting of(ObjectId chatRoomId, String content, ObjectId senderId, ContentType type) {
        return Chatting.builder()
                .senderId(senderId)
                .content(content)
                .chatRoomId(chatRoomId)
                .type(type)
                .build();
    }
}
