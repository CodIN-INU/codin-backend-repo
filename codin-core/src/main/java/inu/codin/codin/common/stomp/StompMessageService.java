package inu.codin.codin.common.stomp;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.domain.chat.chatroom.entity.ChatRoom;
import inu.codin.codin.domain.chat.chatroom.repository.ChatRoomRepository;
import inu.codin.codin.domain.chat.chatting.dto.event.UpdateUnreadCountEvent;
import inu.codin.codin.domain.chat.chatting.entity.Chatting;
import inu.codin.codin.domain.chat.chatting.repository.ChattingRepository;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class StompMessageService {

    private final Map<String, String> sessionStore = new ConcurrentHashMap<>();

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final ChattingRepository chattingRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void connectSession(StompHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        sessionStore.put(sessionId, "");
        log.info("[STOMP CONNECT] session 연결 : {}", sessionId);
    }

    public void enterToChatRoom(StompHeaderAccessor headerAccessor){
        Result result = getResult(headerAccessor);
        sessionStore.put(headerAccessor.getSessionId(), result.chatroom().get_id().toString());
        log.info("[STOMP SUBSCRIBE] session : {}, chatRoomId : {} ", headerAccessor.getSessionId(), result.chatroom().get_id().toString());

        List<Chatting> chattings = updateUnreadCount(result.chatroom.get_id(), result.user.get_id());
        result.chatroom.getParticipants().enter(result.user.get_id());
        chatRoomRepository.save(result.chatroom);
        if (!chattings.isEmpty())
            eventPublisher.publishEvent(new UpdateUnreadCountEvent(this, chattings, result.chatroom.get_id().toString()));
    }

    public void exitToChatRoom(StompHeaderAccessor headerAccessor) {
        Result result = getResult(headerAccessor);
        if (result == null) throw new NotFoundException("[exitToChatRoom] UNSUBSCRIBE 실패");
        result.chatroom().getParticipants().exit(result.user().get_id());
        chatRoomRepository.save(result.chatroom());
        log.info("[STOMP UNSUBSCRIBE] session : {}, chatRoomId : {} ", headerAccessor.getSessionId(), result.chatroom().get_id().toString());
    }

    public void disconnectSession(StompHeaderAccessor headerAccessor){
        sessionStore.remove(headerAccessor.getSessionId());
        log.info("[STOMP DISCONNECT] session : {} ", headerAccessor.getSessionId());

    }

    private Result getResult(StompHeaderAccessor headerAccessor) {
        String email = null;
        if (headerAccessor.getUser() != null) email = headerAccessor.getUser().getName();
        else log.error("헤더에서 유저를 찾을 수 없습니다. command : {}, sessionId : {}", headerAccessor.getCommand(), headerAccessor.getSessionId());

        String chatroomId = headerAccessor.getFirstNativeHeader("chatRoomId");
        if (chatroomId == null || !ObjectId.isValid(chatroomId)) {
            log.error("chatRoomId을 찾을 수 없습니다. command : {}, sessionId : {}, chatRoomId : {}",
                    headerAccessor.getCommand(), headerAccessor.getSessionId(), chatroomId);
            return null;
        } else {
            Optional<ChatRoom> chatroom = chatRoomRepository.findById(new ObjectId(chatroomId));
            if (chatroom.isEmpty()) log.error("채팅방을 찾을 수 없습니다. command : {}, sessionId : {}, chatroomId : {}",
                    headerAccessor.getCommand(), headerAccessor.getSessionId(), chatroomId);
            Optional<UserEntity> user = userRepository.findByEmailAndStatusAll(email);
            if (user.isEmpty()) log.error("유저를 찾을 수 없습니다. command : {}, sessionId : {}, email : {}",
                                headerAccessor.getCommand(), headerAccessor.getSessionId(), email);
            if (chatroom.isPresent() && user.isPresent())
                return new Result(chatroom.get(), user.get());
        }
        return null;
    }



    private record Result(ChatRoom chatroom, UserEntity user) {
    }

    private List<Chatting> updateUnreadCount(ObjectId chatRoomId, ObjectId userId){
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(()-> new NotFoundException("채팅방을 찾을 수 없습니다."));
        List<Chatting> chattings = chattingRepository.findAllByChatRoomIdOrderByCreatedAtDesc(chatRoomId)
                .stream()
                .limit(chatRoom.getParticipants().getInfo().get(userId).getUnreadMessage())
                .map(chatting -> {
                    chatting.minusUnread();
                    return chattingRepository.save(chatting);
                }).toList();
        return chattings;

    }
}
