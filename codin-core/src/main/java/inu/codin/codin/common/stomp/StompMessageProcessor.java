package inu.codin.codin.common.stomp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompMessageProcessor implements ChannelInterceptor {

    private final StompMessageService stompMessageService;

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
        /*
         구독 취소인 경우에는 chatRoomId가 존재하면 방을 나가는 것
         연결 끊기는 세션에서 채팅방 나가기 및 제외하는 로직 추가
         */
        if (headerAccessor.getCommand() == StompCommand.UNSUBSCRIBE || headerAccessor.getCommand() == StompCommand.DISCONNECT) {
            boolean hasChatRoomId = headerAccessor.getSessionAttributes() != null
                    && headerAccessor.getSessionAttributes().containsKey("chatRoomId");

            if (hasChatRoomId) {
                stompMessageService.exitToChatRoom(headerAccessor);
            }
            if (headerAccessor.getCommand() == StompCommand.DISCONNECT) {
                stompMessageService.disconnectSession(headerAccessor);
            }

        } else if (headerAccessor.getDestination() != null && headerAccessor.getDestination().matches("/queue(/unread)?/[^/]+")) {
            switch (headerAccessor.getCommand()) {
                case CONNECT -> stompMessageService.connectSession(headerAccessor);
                case SUBSCRIBE -> stompMessageService.enterToChatRoom(headerAccessor);
            }
        }
    }


}
