package inu.codin.codin.domain.chat.chatroom.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.domain.chat.chatroom.dto.ChatRoomCreateRequestDto;
import inu.codin.codin.domain.chat.chatroom.dto.ChatRoomListResponseDto;
import inu.codin.codin.domain.chat.chatroom.entity.ChatRoom;
import inu.codin.codin.domain.chat.chatroom.exception.ChatRoomCreateFailException;
import inu.codin.codin.domain.chat.chatroom.exception.ChatRoomNotFoundException;
import inu.codin.codin.domain.chat.chatroom.repository.ChatRoomRepository;
import inu.codin.codin.domain.chat.chatting.entity.Chatting;
import inu.codin.codin.domain.chat.chatting.repository.CustomChattingRepository;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.domain.user.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final CustomChattingRepository customChattingRepository;
    private final UserRepository userRepository;

    public Map<String, String> createChatRoom(ChatRoomCreateRequestDto chatRoomCreateRequestDto, UserDetails userDetails) {
        ObjectId senderId = ((CustomUserDetails) userDetails).getId();
        if (senderId.toString().equals(chatRoomCreateRequestDto.getReceiverId())){
            throw new ChatRoomCreateFailException("자기 자신과는 채팅방을 생성할 수 없습니다.");
        }
        log.info("[채팅방 생성 요청] 송신자 ID: {}, 수신자 ID: {}", senderId, chatRoomCreateRequestDto.getReceiverId());

        userRepository.findById(new ObjectId(chatRoomCreateRequestDto.getReceiverId()))
                .orElseThrow(() -> {
                    log.error("[Receive 유저 확인 실패] 수신자 ID: {}를 찾을 수 없습니다.", chatRoomCreateRequestDto.getReceiverId());
                    return new NotFoundException("Receive 유저를 찾을 수 없습니다.");
                });
        log.info("[Receive 유저 확인 완료] 수신자 ID: {}", chatRoomCreateRequestDto.getReceiverId());

        ChatRoom chatRoom = ChatRoom.of(chatRoomCreateRequestDto, senderId);
        chatRoomRepository.save(chatRoom);
        log.info("[채팅방 생성 완료] 채팅방 ID: {}, 송신자 ID: {}, 수신자 ID: {}", chatRoom.get_id(), senderId, chatRoomCreateRequestDto.getReceiverId());

        Map<String, String> response = new HashMap<>();
        response.put("chatRoomId", chatRoom.get_id().toString());
        log.info("[채팅방 생성 응답] 생성된 채팅방 ID: {}", chatRoom.get_id());
        return response;
    }

    public List<ChatRoomListResponseDto> getAllChatRoomByUser(UserDetails userDetails) {
        ObjectId userId = ((CustomUserDetails) userDetails).getId();
        log.info("[유저의 채팅방 조회] 유저 ID: {}", userId);

        List<ChatRoom> chatRooms = chatRoomRepository.findByParticipant(userId);
        log.info("[채팅방 조회 결과] 유저 ID: {}가 참여 중인 채팅방 개수: {}", userId, chatRooms.size());
        return chatRooms.stream()
                .map(chatRoom -> {
                    Chatting chat = customChattingRepository.findMostRecentByChatRoomId(chatRoom.get_id());
                    log.info("[최근 채팅 조회] 채팅방 ID: {}, 최근 채팅 내용: {}", chatRoom.get_id(), chat != null ? chat.getContent() : "없음");
                    return ChatRoomListResponseDto.of(chatRoom, chat);
                }).toList();
    }

    public void leaveChatRoom(String chatRoomId, UserDetails userDetails) {
        ObjectId userId = ((CustomUserDetails) userDetails).getId();
        log.info("[채팅방 탈퇴 요청] 유저 ID: {}, 채팅방 ID: {}", userId, chatRoomId);

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> {
                    log.error("[채팅방 확인 실패] 채팅방 ID: {}를 찾을 수 없습니다.", chatRoomId);
                    return new ChatRoomNotFoundException("채팅방을 찾을 수 없습니다.");
                });
        log.info("[채팅방 확인] 채팅방 ID: {}, 참여자 수: {}", chatRoomId, chatRoom.getParticipants().size());

        boolean isRemoved = chatRoom.getParticipants()
                .removeIf(participant -> participant.getUserId().equals(userId));
        if (!isRemoved) {
            log.warn("[채팅방 탈퇴 실패] 유저 ID: {}는 채팅방에 참여하지 않았습니다.", userId);
            throw new ChatRoomNotFoundException("회원이 포함된 채팅방을 찾을 수 없습니다.");
        }
        log.info("[채팅방 탈퇴 성공] 유저 ID: {}가 채팅방에서 탈퇴", userId);

        if (chatRoom.getParticipants().isEmpty()) {
            chatRoom.delete();
            log.info("[채팅방 삭제] 채팅방 ID: {}에 더 이상 참여자가 없어 채팅방을 삭제합니다.", chatRoomId);
        }
        chatRoomRepository.save(chatRoom);
        log.info("[채팅방 삭제 후 저장] 채팅방 ID: {} 저장 완료", chatRoomId);
        }

    public void setNotificationChatRoom(String chatRoomId, UserDetails userDetails) {
        ObjectId userId = ((CustomUserDetails) userDetails).getId();
        log.info("[알림 설정 요청] 유저 ID: {}, 채팅방 ID: {}", userId, chatRoomId);

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> {
                    log.error("[알림 설정 실패] 채팅방을 찾을 수 없습니다. 채팅방 ID: {}", chatRoomId);
                    return new ChatRoomNotFoundException("채팅방을 찾을 수 없습니다.");
                });
        log.info("[채팅방 확인] 채팅방 ID: {}, 참여자 수: {}", chatRoomId, chatRoom.getParticipants().size());

        chatRoom.getParticipants().stream()
                .filter(participants -> participants.getUserId().equals(userId))
                .forEach(participants -> {
                    participants.updateNotification();
                    log.info("[알림 설정] 유저 ID: {}의 알림 설정 완료", userId);
                });

        chatRoomRepository.save(chatRoom);
        log.info("[알림 설정 완료] 채팅방 ID: {}에 알림 설정 완료", chatRoomId);
    }
}
