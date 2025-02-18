package inu.codin.codin.domain.chat.chatting.dto.event;

import inu.codin.codin.domain.chat.chatting.entity.Chatting;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ChattingArrivedEvent extends ApplicationEvent {

    private final Chatting chatting;

    public ChattingArrivedEvent(Object source, Chatting chatting) {
        super(source);
        this.chatting = chatting;
    }
}
