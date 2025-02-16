package inu.codin.codin.domain.chat.chatroom.service;

import inu.codin.codin.domain.chat.chatroom.dto.event.ChatRoomNotificationEvent;
import inu.codin.codin.domain.chat.chatroom.entity.ParticipantInfo;
import inu.codin.codin.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRoomEventListener {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handleChatRoomNotification(ChatRoomNotificationEvent event){
        if (event.getParticipants().getInfo().containsKey(event.getReceiverId())){
            ParticipantInfo participant = event.getParticipants().getInfo().get(event.getReceiverId());
            if (participant.isNotificationsEnabled())
                notificationService.sendNotificationMessageByChat(participant.getUserId(), event.getChatRoomId());
        }
    }
}
