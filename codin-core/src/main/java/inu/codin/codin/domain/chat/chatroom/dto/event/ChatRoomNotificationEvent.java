package inu.codin.codin.domain.chat.chatroom.dto.event;

import inu.codin.codin.domain.chat.chatroom.entity.Participants;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.context.ApplicationEvent;

@Getter
public class ChatRoomNotificationEvent extends ApplicationEvent {

    private final ObjectId chatRoomId;
    private final ObjectId receiverId;
    private final Participants participants;

    public ChatRoomNotificationEvent(Object source, ObjectId chatRoomId, ObjectId receiverId, Participants participants) {
        super(source);
        this.chatRoomId = chatRoomId;
        this.receiverId = receiverId;
        this.participants = participants;
    }
}
