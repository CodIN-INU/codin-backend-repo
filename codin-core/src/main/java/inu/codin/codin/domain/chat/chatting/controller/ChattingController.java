package inu.codin.codin.domain.chat.chatting.controller;

import inu.codin.codin.common.response.ListResponse;
import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.chat.chatting.dto.request.ChattingRequestDto;
import inu.codin.codin.domain.chat.chatting.dto.response.ChattingResponseDto;
import inu.codin.codin.domain.chat.chatting.service.ChattingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

import java.awt.print.Pageable;

@Controller
@RequiredArgsConstructor
@Tag(name = "Chatting API", description = "채팅 보내기, 채팅 내역 반환")
public class ChattingController {

    private final ChattingService chattingService;

    @Operation(
            summary = "채팅 보내기"
    )
    @MessageMapping("/chats/{chatRoomId}") //앞에 '/pub' 를 붙여서 요청
    @SendTo("/queue/{chatRoomId}")
    public Mono<ResponseEntity<SingleResponse<?>>> sendMessage(@DestinationVariable("chatRoomId") String id, @RequestBody @Valid ChattingRequestDto chattingRequestDto,
                                                               Authentication authentication){
        return chattingService.sendMessage(id, chattingRequestDto, authentication)
                .map( chattingResponseDto -> ResponseEntity.ok().body(new SingleResponse<>(200, "채팅 송신 완료", chattingResponseDto)));
    }


    @Operation(
            summary = "채팅 내용 리스트 가져오기"
    )
    @GetMapping("/chats/list/{chatRoomId}")
    public Mono<ResponseEntity<ListResponse<@Valid ChattingResponseDto>>> getAllMessage(@PathVariable("chatRoomId") String id, Pageable pageable){
        return chattingService.getAllMessage(id, pageable)
                .map(chattingList -> ResponseEntity.ok().body(new ListResponse<>(200, "채팅 내용 리스트 반환 완료", chattingList)));
    }

    //채팅 테스트를 위한 MVC
    @GetMapping("/chat")
    public String chatHtml(){
        return "chat";
    }

}
