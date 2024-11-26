package inu.codin.codin.domain.chat.chatroom.repository;

import inu.codin.codin.domain.chat.chatroom.entity.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {

    @Query("{ 'participants': ?0 }")
    List<ChatRoom> findByParticipant(String userId);
}
