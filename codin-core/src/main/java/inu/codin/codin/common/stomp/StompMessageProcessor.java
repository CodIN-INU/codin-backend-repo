package inu.codin.codin.common.stomp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Objects;

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

        switch (headerAccessor.getCommand()) {
            case CONNECT -> stompMessageService.connectSession(headerAccessor);
            case SUBSCRIBE -> {
                if (Objects.requireNonNull(headerAccessor.getDestination()).matches("/queue(/unread)?/[^/]+"))
                    //헤더에 chatRoomId가 필요한 destination
                    stompMessageService.enterToChatRoom(headerAccessor);
            }
            case UNSUBSCRIBE -> stompMessageService.exitToChatRoom(headerAccessor); //채팅방에서 끊긴 상태
            case DISCONNECT -> stompMessageService.disconnectSession(headerAccessor);  //stomp 세션 끊기
        }
    }


}
