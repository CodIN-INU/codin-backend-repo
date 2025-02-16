package inu.codin.codin.domain.chat;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.exception.JwtException;
import inu.codin.codin.common.security.exception.SecurityErrorCode;
import inu.codin.codin.common.security.jwt.JwtTokenProvider;
import inu.codin.codin.domain.chat.chatroom.entity.ChatRoom;
import inu.codin.codin.domain.chat.chatroom.repository.ChatRoomRepository;
import inu.codin.codin.domain.chat.chatting.service.ChattingService;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.domain.user.security.CustomUserDetailsService;
import io.jsonwebtoken.MalformedJwtException;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompMessageProcessor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final ChattingService chattingService;
    private final Map<String, String> sessionStore = new ConcurrentHashMap<>();
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private static final String BEARER_PREFIX="Bearer ";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel messageChannel){
        StompHeaderAccessor headerAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        handleMessage(headerAccessor);
        return message;
    }

    public void handleMessage(StompHeaderAccessor headerAccessor){
        if (headerAccessor == null || headerAccessor.getCommand() == null){
            throw new MessageDeliveryException(HttpStatus.BAD_REQUEST.toString());
        }

        switch (headerAccessor.getCommand()){
            case CONNECT -> {
                getTokenByHeader(headerAccessor);
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
        String studentId;
        if (headerAccessor.getUser() != null) {
            studentId = headerAccessor.getUser().getName();
        } else {
            throw new UsernameNotFoundException("헤더에서 유저를 찾을 수 없습니다.");
        }
        String chatroomId = sessionStore.get(headerAccessor.getSessionId());
        if (chatroomId == null || !ObjectId.isValid(chatroomId)) {
            throw new IllegalArgumentException("올바른 chatRoomId가 아닙니다: " + chatroomId);
        }
        ChatRoom chatroom = chatRoomRepository.findById(new ObjectId(chatroomId))
                .orElseThrow(() -> new NotFoundException("채팅방을 찾을 수 없습니다."));
        UserEntity user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));
        Result result = new Result(chatroom, user);
        return result;
    }

    private record Result(ChatRoom chatroom, UserEntity user) {
    }

    private void getTokenByHeader(StompHeaderAccessor headerAccessor) {
        // 헤더 토큰 얻기
        String authorizationHeader = String.valueOf(headerAccessor.getNativeHeader("Authorization"));
        if (authorizationHeader == null || authorizationHeader.equals("null")) {
            throw new MalformedJwtException("[Chatting] JWT를 찾을 수 없습니다.");
        }
        String token = authorizationHeader.substring(BEARER_PREFIX.length());

        // 토큰 인증
        try {
            if (jwtTokenProvider.validateAccessToken(token)) {
                String studentId = jwtTokenProvider.getUsername(token);
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(studentId);
                headerAccessor.setUser(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
            }
        } catch (MessageDeliveryException e) {
            throw new MessageDeliveryException("[Chatting] Jwt로 인한 메세지 전송 오류입니다.");
        } catch (MalformedJwtException e) {
            throw new MalformedJwtException("[Chatting] 비정상적인 jwt 토큰 입니다.");
        } catch (Exception e) {
            throw new JwtException(SecurityErrorCode.INVALID_TOKEN, "[Chatting] 인증되지 않은 jwt 토큰 입니다.");
        }
    }
}
