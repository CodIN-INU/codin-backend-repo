package inu.codin.codin.domain.chat.chatroom.entity;

import inu.codin.codin.common.BaseTimeEntity;
import inu.codin.codin.domain.chat.chatroom.dto.ChatRoomCreateRequestDto;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "chatroom")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ChatRoom extends BaseTimeEntity {

    @Id @NotBlank
    private ObjectId _id;

    @NotBlank
    private String roomName;

    @NotBlank
    private List<Participants> participants; //참가자들의 userId (1:1 채팅에서는 두 명의 id만 들어감)


    @Builder
    public ChatRoom(String roomName, List<Participants> participants) {
        this.roomName = roomName;
        this.participants = participants;
    }

    public static ChatRoom of(ChatRoomCreateRequestDto chatRoomCreateRequestDto, ObjectId senderId){
        ArrayList<Participants> participants = new ArrayList<>(2);
        participants.add(new Participants(new ObjectId(chatRoomCreateRequestDto.getReceiverId()), true));
        participants.add(new Participants(senderId, true));
        return ChatRoom.builder()
                .roomName(chatRoomCreateRequestDto.getRoomName())
                .participants(participants)
                .build();
    }
}
