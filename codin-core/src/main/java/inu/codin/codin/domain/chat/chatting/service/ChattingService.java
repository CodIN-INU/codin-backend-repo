package inu.codin.codin.domain.chat.chatting.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.chat.chatroom.entity.ChatRoom;
import inu.codin.codin.domain.chat.chatroom.exception.ChatRoomNotFoundException;
import inu.codin.codin.domain.chat.chatroom.repository.ChatRoomRepository;
import inu.codin.codin.domain.chat.chatroom.service.ChatRoomService;
import inu.codin.codin.domain.chat.chatting.dto.event.ChattingArrivedEvent;
import inu.codin.codin.domain.chat.chatting.dto.request.ChattingRequestDto;
import inu.codin.codin.domain.chat.chatting.dto.response.ChattingAndUserIdResponseDto;
import inu.codin.codin.domain.chat.chatting.dto.response.ChattingResponseDto;
import inu.codin.codin.domain.chat.chatting.entity.Chatting;
import inu.codin.codin.domain.chat.chatting.repository.ChattingRepository;
import inu.codin.codin.domain.notification.service.NotificationService;
import inu.codin.codin.domain.user.security.CustomUserDetails;
import inu.codin.codin.infra.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChattingService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChattingRepository chattingRepository;
    private final S3Service s3Service;
    private final NotificationService notificationService;
    private final ChatRoomService chatRoomService;
    private final ApplicationEventPublisher eventPublisher;

    public ChattingResponseDto sendMessage(String id, ChattingRequestDto chattingRequestDto, Authentication authentication) {
        ChatRoom chatRoom = chatRoomRepository.findById(new ObjectId(id))
                .orElseThrow(() -> {
                    log.warn("[채팅방 조회 실패] 채팅방 ID: {}를 찾을 수 없습니다.", id);
                    return new ChatRoomNotFoundException("채팅방을 찾을 수 없습니다.");
                });

        ObjectId userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        Integer countOfParticipating = chatRoomService.countOfParticipating(chatRoom.get_id());
        Chatting chatting = Chatting.of(chatRoom.get_id(), chattingRequestDto, userId,
                chatRoom.getParticipants().getInfo().size()-countOfParticipating);

        log.info("[메시지 전송 성공] 메시지: [{}], 송신자 ID: {}, 채팅방 ID: {}", chattingRequestDto.getContent(), userId, id);

        chattingRepository.save(chatting);

//        //Receiver의 알림 체크 후, 메세지 전송
//        for (Participants participant : chatRoom.getParticipants()){
//            if (participant.getUserId() != userId && participant.isNotificationsEnabled()){
//                notificationService.sendNotificationMessageByChat(participant.getUserId(), chattingRequestDto, chatRoom);
//            }
//        }
        eventPublisher.publishEvent(new ChattingArrivedEvent(this, chatting));

        return ChattingResponseDto.of(chatting);
    }

    public ChattingAndUserIdResponseDto getAllMessage(String id, int page) {
            log.info("[메시지 조회] 채팅방 ID: {}, 페이지: {}", id, page);

            Pageable pageable = PageRequest.of(page, 20, Sort.by("createdAt").descending());
            chatRoomRepository.findById(new ObjectId(id))
                    .orElseThrow(() -> {
                        log.error("[채팅방 조회 실패] 채팅방 ID: {}를 찾을 수 없습니다.", id);
                        return new ChatRoomNotFoundException("채팅방을 찾을 수 없습니다.");
                    });

            List<ChattingResponseDto> chattingResponseDto = chattingRepository.findAllByChatRoomId(new ObjectId(id), pageable)
                    .stream().map(ChattingResponseDto::of).toList();

            log.info("[메시지 조회 성공] 채팅방 ID: {}, 메시지 개수: {}", id, chattingResponseDto.size());

            return new ChattingAndUserIdResponseDto(chattingResponseDto, SecurityUtils.getCurrentUserId().toString());
    }

    public List<String> sendImageMessage(List<MultipartFile> chatImages) {
        log.info("[이미지 메시지 전송] 이미지 개수: {}", chatImages.size());

        List<String> imageUrls = s3Service.handleImageUpload(chatImages);

        log.info("[이미지 메시지 전송 성공] 업로드된 이미지 URL 개수: {}", imageUrls.size());

        return imageUrls;
    }

    public void updateUnreadCount(ObjectId chatRoomId, ObjectId userId){
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                        .orElseThrow(()-> new NotFoundException("채팅방을 찾을 수 없습니다."));
        chattingRepository.findAllByChatRoomIdOrderByCreatedAtDesc(chatRoomId)
                .stream()
                .limit(chatRoom.getParticipants().getInfo().get(userId).getUnreadMessage())
                .forEach(chatting -> {
                    chatting.minusUnread();
                    chattingRepository.save(chatting);
                });

    }
}
