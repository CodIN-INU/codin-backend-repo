package inu.codin.codin.domain.chat.chatting.entity;

import inu.codin.codin.common.dto.BaseTimeEntity;
import inu.codin.codin.domain.chat.chatting.dto.ContentType;
import inu.codin.codin.domain.chat.chatting.dto.request.ChattingRequestDto;
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

    private ContentType contentType;

    private int unreadCount;

    @Builder
    public Chatting(ObjectId senderId, String content, ObjectId chatRoomId, ContentType contentType, int unreadCount) {
        this.senderId = senderId;
        this.content = content;
        this.chatRoomId = chatRoomId;
        this.contentType = contentType;
        this.unreadCount = unreadCount;
    }

    public static Chatting of(ObjectId chatRoomId, ChattingRequestDto chattingRequestDto, ObjectId senderId, int unreadCount) {
        return Chatting.builder()
                .senderId(senderId)
                .content(chattingRequestDto.getContent())
                .chatRoomId(chatRoomId)
                .contentType(chattingRequestDto.getContentType())
                .unreadCount(unreadCount)
                .build();
    }

    public void minusUnread(){
        this.unreadCount--;
    }
}
