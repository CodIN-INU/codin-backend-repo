package inu.codin.codin.domain.chat.chatting.repository;

import inu.codin.codin.domain.chat.chatting.entity.Chatting;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChattingRepository extends MongoRepository<Chatting, String> {

    List<Chatting> findAllByChatRoomIdOrderByCreatedAtDesc(ObjectId chatRoomId);

    List<Chatting> findAllByChatRoomId(ObjectId id, Pageable pageable);
}
