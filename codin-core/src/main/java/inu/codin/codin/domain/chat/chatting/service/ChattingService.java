package inu.codin.codin.domain.chat.chatting.service;

import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.chat.chatroom.entity.ChatRoom;
import inu.codin.codin.domain.chat.chatroom.exception.ChatRoomNotFoundException;
import inu.codin.codin.domain.chat.chatroom.repository.ChatRoomRepository;
import inu.codin.codin.domain.chat.chatting.dto.request.ChattingRequestDto;
import inu.codin.codin.domain.chat.chatting.dto.response.ChattingAndUserIdResponseDto;
import inu.codin.codin.domain.chat.chatting.dto.response.ChattingResponseDto;
import inu.codin.codin.domain.chat.chatting.entity.Chatting;
import inu.codin.codin.domain.chat.chatting.repository.ChattingRepository;
import inu.codin.codin.domain.user.security.CustomUserDetails;
import inu.codin.codin.infra.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChattingService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChattingRepository chattingRepository;
    private final S3Service s3Service;

    public ChattingResponseDto sendMessage(String id, ChattingRequestDto chattingRequestDto, Authentication authentication) {
        ChatRoom chatRoom = chatRoomRepository.findById(new ObjectId(id))
                .orElseThrow(() -> new ChatRoomNotFoundException("채팅방을 찾을 수 없습니다."));
        ObjectId userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        Chatting chatting = Chatting.of(chatRoom.get_id(), chattingRequestDto.getContent(), userId, chattingRequestDto.getContentType());
        log.info("[sendMessage] [{}]  (유저 {} , 채팅방 {})", chattingRequestDto.getContent(), userId, id);
        chattingRepository.save(chatting);
        return ChattingResponseDto.of(chatting);
    }

    public ChattingAndUserIdResponseDto getAllMessage(String id, int page) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by("createdAt").descending());
        chatRoomRepository.findById(new ObjectId(id))
                .orElseThrow(() -> new ChatRoomNotFoundException("채팅방을 찾을 수 없습니다."));
        List<ChattingResponseDto> chattingResponseDto = chattingRepository.findAllByChatRoomIdOrderByCreatedAt(new ObjectId(id), pageable)
                .stream().map(ChattingResponseDto::of).toList();
        log.info("[getAllMessage] 유저 {}, 채팅방 {}의 메세지 내역(page = {}) 반환", SecurityUtils.getCurrentUserId().toString(), id, page);
        return new ChattingAndUserIdResponseDto(chattingResponseDto, SecurityUtils.getCurrentUserId().toString());
    }

    public List<String> sendImageMessage(List<MultipartFile> chatImages) {
        log.info("[sendImageMessage] 이미지 업로드");
        return s3Service.handleImageUpload(chatImages);
    }
}
