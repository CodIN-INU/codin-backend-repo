package inu.codin.codin.domain.chat.chatting.service;

import inu.codin.codin.domain.chat.chatroom.entity.ChatRoom;
import inu.codin.codin.domain.chat.chatroom.exception.ChatRoomNotFoundException;
import inu.codin.codin.domain.chat.chatroom.repository.ChatRoomRepository;
import inu.codin.codin.domain.chat.chatting.dto.request.ChattingRequestDto;
import inu.codin.codin.domain.chat.chatting.dto.response.ChattingResponseDto;
import inu.codin.codin.domain.chat.chatting.entity.Chatting;
import inu.codin.codin.domain.chat.chatting.exception.ChattingNotFoundException;
import inu.codin.codin.domain.chat.chatting.repository.ChattingRepository;
import inu.codin.codin.domain.user.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChattingService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChattingRepository chattingRepository;

    //todo 이미지 채팅에 따른 S3 처리

    public Mono<ChattingResponseDto> sendMessage(String id, ChattingRequestDto chattingRequestDto, Authentication authentication) {
        ChatRoom chatRoom = chatRoomRepository.findById(id)
                .orElseThrow(() -> new ChatRoomNotFoundException("채팅방을 찾을 수 없습니다."));
        String userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        Chatting chatting = Chatting.of(chatRoom.get_id(), chattingRequestDto, new ObjectId(userId));
        log.info("Message [{}] send by member: {} to chatting room: {}", chattingRequestDto.getContent(), userId, id);
        return chattingRepository.save(chatting).map(ChattingResponseDto::of);
    }

    public Mono<List<ChattingResponseDto>> getAllMessage(String id) {
        chatRoomRepository.findById(id)
                .orElseThrow(() -> new ChatRoomNotFoundException("채팅방을 찾을 수 없습니다."));
        return chattingRepository.findAllByChatRoomId(id)
                .switchIfEmpty(Mono.error(new ChattingNotFoundException("채팅 내역을 찾을 수 없습니다.")))
                .map(ChattingResponseDto::of)
                .collectList();
    }
}
