package inu.codin.codin.common.stomp;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.domain.chat.chatroom.entity.ChatRoom;
import inu.codin.codin.domain.chat.chatroom.repository.ChatRoomRepository;
import inu.codin.codin.domain.chat.chatting.service.ChattingService;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompMessageProcessor implements ChannelInterceptor {

    private final ChattingService chattingService;
    private final Map<String, String> sessionStore = new ConcurrentHashMap<>();
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    private final HttpServletRequest request;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel messageChannel){
        StompHeaderAccessor headerAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        handleMessage(headerAccessor, request);
        return message;
    }

    public void handleMessage(StompHeaderAccessor headerAccessor, HttpServletRequest request){
        if (headerAccessor == null || headerAccessor.getCommand() == null){
            throw new MessageDeliveryException(HttpStatus.BAD_REQUEST.toString());
        }

        switch (headerAccessor.getCommand()){
            case CONNECT -> {
                connectSession(headerAccessor);
            }
            case SUBSCRIBE -> {
                log.info("[STOMP] Subscribe" );
                enterToChatRoom(headerAccessor);
            }
            case UNSUBSCRIBE -> {
                log.info("[STOMP] UnSubscribe" );
                exitToChatRoom(headerAccessor);
            }
            case DISCONNECT -> {
                log.info("[STOMP] DISCONNECT");
                exitToChatRoom(headerAccessor);
                disconnectSession(headerAccessor);
            }
        }
    }

    @EventListener
    private void connectSession(StompHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String chatroomId = headerAccessor.getFirstNativeHeader("chatRoomId");
        sessionStore.put(sessionId, chatroomId);
    }
    private void exitToChatRoom(StompHeaderAccessor headerAccessor) {
        Result result = getResult(headerAccessor);
        result.chatroom().getParticipants().exit(result.user().get_id());
        chatRoomRepository.save(result.chatroom());
    }

    private void enterToChatRoom(StompHeaderAccessor headerAccessor){
        Result result = getResult(headerAccessor);
        chattingService.updateUnreadCount(result.chatroom.get_id(), result.user.get_id());
        result.chatroom.getParticipants().enter(result.user.get_id());
        chatRoomRepository.save(result.chatroom);
    }

    private void disconnectSession(StompHeaderAccessor headerAccessor){
        sessionStore.remove(headerAccessor.getSessionId());
    }

    private Result getResult(StompHeaderAccessor headerAccessor) {
        String email;
        if (headerAccessor.getUser() != null) {
            email = headerAccessor.getUser().getName();
        } else {
            throw new UsernameNotFoundException("헤더에서 유저를 찾을 수 없습니다.");
        }
        String chatroomId = sessionStore.get(headerAccessor.getSessionId());
        if (chatroomId == null || !ObjectId.isValid(chatroomId)) {
            throw new IllegalArgumentException("세션에서 가져올 수 없거나, 올바른 chatRoomId가 아닙니다: " + chatroomId);
        }
        ChatRoom chatroom = chatRoomRepository.findById(new ObjectId(chatroomId))
                .orElseThrow(() -> new NotFoundException("채팅방을 찾을 수 없습니다."));
        UserEntity user = userRepository.findByEmailAndDisabledAndActive(email)
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));
        Result result = new Result(chatroom, user);
        return result;
    }

    private record Result(ChatRoom chatroom, UserEntity user) {
    }
}
