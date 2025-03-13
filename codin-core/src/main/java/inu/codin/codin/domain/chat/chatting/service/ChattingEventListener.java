package inu.codin.codin.domain.chat.chatting.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.domain.chat.chatroom.entity.ChatRoom;
import inu.codin.codin.domain.chat.chatroom.entity.Participants;
import inu.codin.codin.domain.chat.chatroom.repository.ChatRoomRepository;
import inu.codin.codin.domain.chat.chatting.dto.event.ChattingArrivedEvent;
import inu.codin.codin.domain.chat.chatting.dto.event.ChattingNotificationEvent;
import inu.codin.codin.domain.chat.chatting.dto.event.UpdateUnreadCountEvent;
import inu.codin.codin.domain.chat.chatting.entity.Chatting;
import inu.codin.codin.domain.chat.chatting.repository.ChattingRepository;
import inu.codin.codin.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChattingEventListener {

    private final ChatRoomRepository chatRoomRepository;
    private final ChattingRepository chattingRepository;
    private final SimpMessageSendingOperations template;
    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handleChattingArrivedEvent(ChattingArrivedEvent event){
        Chatting chatting = event.getChatting();
        ChatRoom chatRoom = chatRoomRepository.findById(chatting.getChatRoomId())
                .orElseThrow(() -> new NotFoundException("채팅방을 찾을 수 없습니다. ID: "+ chatting.getChatRoomId()));
        chatRoom.getParticipants().getInfo().forEach(
                (id, participantInfo) -> {
                    if (!participantInfo.isConnected()) {
                        participantInfo.plusUnread();
                    }
                }
        );
        chatRoom.updateLastMessage(chatting.getContent());
        chatRoomRepository.save(chatRoom);
        chattingRepository.save(chatting);

    }

    @Async
    @EventListener
    public void handleChattingNotificationEvent(ChattingNotificationEvent event){
        event.getChatRoom().getParticipants().getInfo().values().stream()
                .filter(participantInfo -> !participantInfo.getUserId().equals(event.getUserId()) && participantInfo.isNotificationsEnabled())
                .peek(participantInfo -> notificationService.sendNotificationMessageByChat(participantInfo.getUserId(), event.getChatRoom().get_id()));
    }


    @EventListener
    public void updateUnreadCountEvent(UpdateUnreadCountEvent updateUnreadCountEvent){
        List<Map<String, String>> result = new ArrayList<>();
        for (Chatting chat : updateUnreadCountEvent.getChattingList()){
            Map<String, String> payload = Map.of(
                    "id", chat.get_id().toString(),
                    "unread", String.valueOf(chat.getUnreadCount())
            );
            result.add(payload);
        }

        template.convertAndSend("/queue/unread/"+ updateUnreadCountEvent.getChatRoomId(), result);

    }

}
