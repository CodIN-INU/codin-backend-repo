package inu.codin.codin.domain.chat.chatting.repository;

import inu.codin.codin.domain.chat.chatting.entity.Chatting;
import org.bson.types.ObjectId;
import reactor.core.publisher.Mono;

public interface CustomChattingRepository {

    Mono<Chatting> findRecentMessageByChatRoomId(ObjectId id);

}
