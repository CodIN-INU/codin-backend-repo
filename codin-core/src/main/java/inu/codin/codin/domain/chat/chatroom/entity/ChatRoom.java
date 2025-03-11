package inu.codin.codin.domain.chat.chatroom.entity;

import inu.codin.codin.common.dto.BaseTimeEntity;
import inu.codin.codin.domain.chat.chatroom.dto.ChatRoomCreateRequestDto;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "chatroom")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ChatRoom extends BaseTimeEntity {

    @Id @NotBlank
    private ObjectId _id;

    @NotBlank
    private String roomName;

    @NotBlank
    private ObjectId referenceId; //채팅방이 시작한 곳의 id

    @NotBlank
    private Participants participants; //참가자들의 userId (1:1 채팅에서는 두 명의 id만 들어감)

    private String lastMessage;


    @Builder
    public ChatRoom(String roomName, ObjectId referenceId, Participants participants, String lastMessage) {
        this.roomName = roomName;
        this.referenceId = referenceId;
        this.participants = participants;
        this.lastMessage = lastMessage;
    }

    public static ChatRoom of(ChatRoomCreateRequestDto chatRoomCreateRequestDto, ObjectId senderId){
        Participants participants = new Participants();
        participants.create(senderId);
        participants.create(new ObjectId(chatRoomCreateRequestDto.getReceiverId()));
        return ChatRoom.builder()
                .roomName(chatRoomCreateRequestDto.getRoomName())
                .referenceId(new ObjectId(chatRoomCreateRequestDto.getReferenceId()))
                .participants(participants)
                .build();
    }

    public void updateLastMessage(String message){
        this.lastMessage = message;
    }
}
