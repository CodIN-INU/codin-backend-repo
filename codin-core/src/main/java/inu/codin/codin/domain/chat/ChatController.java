package inu.codin.codin.domain.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/{chatRoomId}") //앞에 '/pub' 를 붙여서 요청
    @SendTo("/topic/{chatRoomId}")
    public void chat(@DestinationVariable("chatRoomId") String id, @RequestBody ChatRequestDto chatRequestDto){
        log.info("Message [{}] send by member: {} to chatting room: {}", chatRequestDto.getContent(), chatRequestDto.getSenderId(), id);
    }
}
