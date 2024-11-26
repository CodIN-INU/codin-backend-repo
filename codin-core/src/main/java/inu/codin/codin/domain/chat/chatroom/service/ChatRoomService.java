package inu.codin.codin.domain.chat.chatroom.service;

import inu.codin.codin.domain.chat.chatroom.exception.ChatRoomNotFoundException;
import inu.codin.codin.domain.chat.chatroom.dto.ChatRoomCreateRequestDto;
import inu.codin.codin.domain.chat.chatroom.dto.ChatRoomListResponseDto;
import inu.codin.codin.domain.chat.chatroom.entity.ChatRoom;
import inu.codin.codin.domain.chat.chatroom.entity.Participants;
import inu.codin.codin.domain.chat.chatroom.repository.ChatRoomRepository;
import inu.codin.codin.domain.user.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    public void createChatRoom(ChatRoomCreateRequestDto chatRoomCreateRequestDto, UserDetails userDetails) {
        String senderId = ((CustomUserDetails) userDetails).getId();
        ChatRoom chatRoom = ChatRoom.of(chatRoomCreateRequestDto, senderId);
        chatRoomRepository.save(chatRoom);
    }

    public List<ChatRoomListResponseDto> getAllChatRoomByUser(UserDetails userDetails) {
        String userId = ((CustomUserDetails) userDetails).getId();
        List<ChatRoom> chatRooms = chatRoomRepository.findByParticipant(userId);
        return chatRooms.stream().map(ChatRoomListResponseDto::of).toList();
    }

    public void leaveChatRoom(String chatRoomId, UserDetails userDetails) {
        String userId = ((CustomUserDetails) userDetails).getId();
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatRoomNotFoundException("채팅방을 찾을 수 없습니다."));
        chatRoom.getParticipants().remove(userId);
        if (chatRoom.getParticipants().isEmpty()) {
            chatRoom.delete();
            log.info("[LeaveChatRoom] 채팅방에 참여자가 없어 채팅방 삭제");
        }
        chatRoomRepository.save(chatRoom);
    }

    public void setNotificationChatRoom(String chatRoomId, UserDetails userDetails) {
        String userId = ((CustomUserDetails) userDetails).getId();
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatRoomNotFoundException("채팅방을 찾을 수 없습니다."));
        chatRoom.getParticipants().stream()
                .filter(participants -> participants.getUserId().equals(userId))
                .forEach(Participants::updateNotification);
        chatRoomRepository.save(chatRoom);
    }
}
