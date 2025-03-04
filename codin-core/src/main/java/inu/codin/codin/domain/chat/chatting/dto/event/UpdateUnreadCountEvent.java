package inu.codin.codin.domain.chat.chatting.dto.event;

import inu.codin.codin.domain.chat.chatting.entity.Chatting;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class UpdateUnreadCountEvent extends ApplicationEvent {

    private final List<Chatting> chattingList;
    private final String chatRoomId;

    public UpdateUnreadCountEvent(Object source, List<Chatting> chattingList, String chatRoomId) {
        super(source);
        this.chattingList = chattingList;
        this.chatRoomId = chatRoomId;
    }
}
