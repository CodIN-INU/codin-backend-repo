package inu.codin.codin.domain.chat.chatting;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChattingController {

    @MessageMapping("/chats/{chatRoomId}") //앞에 '/pub' 를 붙여서 요청
    @SendTo("/queue/{chatRoomId}")
    public void chat(@DestinationVariable("chatRoomId") String id, @RequestBody ChattingRequestDto chattingRequestDto){
        log.info("Message [{}] send by member: {} to chatting room: {}", chattingRequestDto.getContent(), chattingRequestDto.getSenderId(), id);
    }


}
