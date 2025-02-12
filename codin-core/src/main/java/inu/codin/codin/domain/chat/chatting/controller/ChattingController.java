package inu.codin.codin.domain.chat.chatting.controller;

import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.chat.chatting.dto.request.ChattingRequestDto;
import inu.codin.codin.domain.chat.chatting.service.ChattingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
    public ResponseEntity<SingleResponse<?>> sendMessage(@DestinationVariable("chatRoomId") String id, @RequestBody @Valid ChattingRequestDto chattingRequestDto,
                                                               @AuthenticationPrincipal Authentication authentication){
        return ResponseEntity.ok().body(new SingleResponse<>(200, "채팅 송신 완료", chattingService.sendMessage(id, chattingRequestDto, authentication)));
    }

    @Operation(
            summary = "채팅으로 사진 보내기"
    )
    @PostMapping(value = "/chats/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SingleResponse<?>> sendImageMessage(List<MultipartFile> chatImages){
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "채팅 사진 업로드 완료", chattingService.sendImageMessage(chatImages)));
    }

    @Operation(
            summary = "채팅 내용 리스트 가져오기",
            description = "Pageable에 해당하는 page, size, sort 내역에 맞게 반환"
    )
    @GetMapping("/chats/list/{chatRoomId}")
    public ResponseEntity<SingleResponse<?>> getAllMessage(@PathVariable("chatRoomId") String id,
                                                                                        @RequestParam("page") int page){
        return ResponseEntity.ok().body(new SingleResponse<>(200, "채팅 내용 리스트 반환 완료", chattingService.getAllMessage(id, page)));
    }

    //채팅 테스트를 위한 MVC
    @GetMapping("/chat")
    public String chatHtml(){
        return "chat";
    }

    @GetMapping("/chat/image")
    public String chatImageHtml(){
        return "chatImage";
    }

}
