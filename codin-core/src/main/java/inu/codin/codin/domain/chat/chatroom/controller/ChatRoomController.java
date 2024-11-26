package inu.codin.codin.domain.chat.chatroom.controller;

import inu.codin.codin.common.ResponseUtils;
import inu.codin.codin.domain.chat.chatroom.dto.ChatRoomListResponseDto;
import inu.codin.codin.domain.chat.chatroom.service.ChatRoomService;
import inu.codin.codin.domain.chat.chatroom.dto.ChatRoomCreateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/chats")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @Operation(
            summary = "채팅방 생성"
    )
    @PostMapping
    public ResponseEntity<?> createChatRoom(@RequestBody ChatRoomCreateRequestDto chatRoomCreateRequestDto, @AuthenticationPrincipal UserDetails userDetails){
        chatRoomService.createChatRoom(chatRoomCreateRequestDto, userDetails);
        return ResponseUtils.successMsg("채팅방 생성 완료");
    }

    @Operation(
            summary = "사용자가 포함된 모든 채팅방 리스트 반환"
    )
    @GetMapping
    public ResponseEntity<List<ChatRoomListResponseDto>> getAllChatRoomByUser(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseUtils.success(chatRoomService.getAllChatRoomByUser(userDetails));
    }

    @Operation(
            summary = "채팅방 나가기"
    )
    @DeleteMapping("/{chatRoomId}")
    public ResponseEntity<?> leaveChatRoom(@PathVariable("chatRoomId") String chatRoomId, @AuthenticationPrincipal UserDetails userDetails){
        chatRoomService.leaveChatRoom(chatRoomId, userDetails);
        return ResponseUtils.successMsg("채팅방 나가기 완료");
    }

    @Operation(
            summary = "채팅방 알림 여부 수정"
    )
    @GetMapping("/notification/{chatRoomId}")
    public ResponseEntity<?> setNotificationChatRoom(@PathVariable("chatRoomId") String chatRoomId, @AuthenticationPrincipal UserDetails userDetails){
        chatRoomService.setNotificationChatRoom(chatRoomId, userDetails);
        return ResponseUtils.successMsg("채팅방 알림 여부 수정 완료");
    }
}
