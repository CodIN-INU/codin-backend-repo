package inu.codin.codin.domain.chat.chatting.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.domain.chat.chatroom.entity.ChatRoom;
import inu.codin.codin.domain.chat.chatroom.entity.ParticipantInfo;
import inu.codin.codin.domain.chat.chatroom.repository.ChatRoomRepository;
import inu.codin.codin.domain.chat.chatting.dto.event.ChattingArrivedEvent;
import inu.codin.codin.domain.chat.chatting.dto.event.ChattingNotificationEvent;
import inu.codin.codin.domain.chat.chatting.dto.event.UpdateUnreadCountEvent;
import inu.codin.codin.domain.chat.chatting.entity.Chatting;
import inu.codin.codin.domain.chat.chatting.repository.ChattingRepository;
import inu.codin.codin.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChattingEventListener {

    private final ChatRoomRepository chatRoomRepository;
    private final ChattingRepository chattingRepository;
    private final SimpMessageSendingOperations template;
    private final NotificationService notificationService;

    /*
        채팅을 발신했을 경우,
        1. 상대방이 접속한 상태가 아니라면 상대방의 unread 값 +1
        2. 채팅방의 마지막 메세지 업데이트
        3. /queue/chatroom/{userId} 를 통해 실시간으로 채팅방 목록 업데이트
     */
    @Async
    @EventListener
    public void handleChattingArrivedEvent(ChattingArrivedEvent event){
        Chatting chatting = event.getChatting();
        ChatRoom chatRoom = chatRoomRepository.findById(chatting.getChatRoomId())
                .orElseThrow(() -> new NotFoundException("채팅방을 찾을 수 없습니다. ID: "+ chatting.getChatRoomId()));

        updateUnread(event, chatRoom);
        chatRoomRepository.save(chatRoom);
        chattingRepository.save(chatting);

    }

    private void updateUnread(ChattingArrivedEvent event, ChatRoom chatRoom) {
        Map<String, Map<String, String>> result = new HashMap<>();
        ObjectId receiverId = null;
        for (Map.Entry<ObjectId, ParticipantInfo> entry : chatRoom.getParticipants().getInfo().entrySet()) {
            ParticipantInfo participantInfo = entry.getValue();

            if (!participantInfo.getUserId().equals(event.getChatting().getSenderId())) {
                receiverId = participantInfo.getUserId();
                if (!participantInfo.isConnected()) {
                    participantInfo.plusUnread();
                    result.put(chatRoom.get_id().toString(), getLastMessageAndUnread(event, participantInfo));
                }
            }
        }
        chatRoom.updateLastMessage(event.getChatting().getContent());
        if (receiverId!=null)
            template.convertAndSend("/queue/chatroom/unread/"+ receiverId, result);
    }

    private static Map<String, String> getLastMessageAndUnread(ChattingArrivedEvent event, ParticipantInfo participantInfo) {
        Map<String, String> lastMessageAndUnread = new HashMap<>();
        lastMessageAndUnread.put("lastMessage", event.getChatting().getContent());
        lastMessageAndUnread.put("unread", String.valueOf(participantInfo.getUnreadMessage()));
        return lastMessageAndUnread;
    }

    @Async
    @EventListener
    public void handleChattingNotificationEvent(ChattingNotificationEvent event){
        event.getChatRoom().getParticipants().getInfo().values().stream()
                .filter(participantInfo -> !participantInfo.getUserId().equals(event.getUserId()) && participantInfo.isNotificationsEnabled())
                .peek(participantInfo -> notificationService.sendNotificationMessageByChat(participantInfo.getUserId(), event.getChatRoom().get_id()));
    }

    /*
        유저가 채팅방 입장 시, 읽지 않은 채팅에 대하여 새로운 unread 값 송신
        클라이언트 : chat_id 와 일치하는 채팅값의 unread 값 업데이트
     */
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
