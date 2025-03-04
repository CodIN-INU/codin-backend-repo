package inu.codin.codin.domain.chat.chatting.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.domain.chat.chatroom.entity.ChatRoom;
import inu.codin.codin.domain.chat.chatroom.repository.ChatRoomRepository;
import inu.codin.codin.domain.chat.chatting.dto.event.ChattingArrivedEvent;
import inu.codin.codin.domain.chat.chatting.entity.Chatting;
import inu.codin.codin.domain.chat.chatting.repository.ChattingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChattingEventListener {

    private final ChatRoomRepository chatRoomRepository;
    private final ChattingRepository chattingRepository;

    @Async
    @EventListener
    public void handleChattingArrivedEvent(ChattingArrivedEvent event){
        Chatting chatting = event.getChatting();
        ChatRoom chatRoom = chatRoomRepository.findById(chatting.getChatRoomId())
                .orElseThrow(() -> new NotFoundException("채팅방을 찾을 수 없습니다. ID: "+ chatting.getChatRoomId()));
        chatRoom.getParticipants().getInfo().forEach(
                (id, participantInfo) -> {
                    if (!participantInfo.isConnected()) {
                        participantInfo.plusUnread();
                    }
                }
        );
        chatRoom.updateLastMessage(chatting.getContent());
        chatRoomRepository.save(chatRoom);
        chattingRepository.save(chatting);

    }

}
