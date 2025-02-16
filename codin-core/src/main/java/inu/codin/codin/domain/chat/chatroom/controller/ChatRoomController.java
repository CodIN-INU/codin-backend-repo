package inu.codin.codin.domain.chat.chatroom.controller;

import inu.codin.codin.common.response.ListResponse;
import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.chat.chatroom.dto.ChatRoomCreateRequestDto;
import inu.codin.codin.domain.chat.chatroom.dto.ChatRoomListResponseDto;
import inu.codin.codin.domain.chat.chatroom.service.ChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/chatroom")
@Tag(name = "ChatRoom API", description = "채팅방 생성, 리스트 반환, 채팅방 나가기, 채팅방 알림 설정")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @Operation(
            summary = "채팅방 생성"
    )
    @PostMapping
    public ResponseEntity<SingleResponse<?>> createChatRoom(@RequestBody ChatRoomCreateRequestDto chatRoomCreateRequestDto){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SingleResponse<>(201, "채팅방 생성 완료", chatRoomService.createChatRoom(chatRoomCreateRequestDto)));
    }

    @Operation(
            summary = "사용자가 포함된 모든 채팅방 리스트 반환"
    )
    @GetMapping
    public ResponseEntity<ListResponse<ChatRoomListResponseDto>> getAllChatRoomByUser(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok()
                .body(new ListResponse<>(200, "채팅방 리스트 반환 완료", chatRoomService.getAllChatRoomByUser(userDetails)));
    }

    @Operation(
            summary = "채팅방 나가기"
    )
    @DeleteMapping("/{chatRoomId}")
    public ResponseEntity<SingleResponse<?>> leaveChatRoom(@PathVariable("chatRoomId") String chatRoomId){
        chatRoomService.leaveChatRoom(chatRoomId);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "채팅방 나가기 완료", null));
    }

    @Operation(
            summary = "채팅방 알림 여부 수정"
    )
    @GetMapping("/notification/{chatRoomId}")
    public ResponseEntity<SingleResponse<?>> setNotificationChatRoom(@PathVariable("chatRoomId") String chatRoomId){
        chatRoomService.setNotificationChatRoom(chatRoomId);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "채팅방 알림 여부 수정 완료", null));
    }
}
