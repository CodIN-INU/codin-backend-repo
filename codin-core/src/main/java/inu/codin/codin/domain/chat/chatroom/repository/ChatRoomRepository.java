package inu.codin.codin.domain.chat.chatroom.repository;

import inu.codin.codin.domain.chat.chatroom.entity.ChatRoom;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {

    @Query("{ '_id': ?0, 'deletedAt': null }")
    Optional<ChatRoom> findById(ObjectId id);

    @Query("{ 'participants': { '$elemMatch': { 'userId': ?0 } } , 'deleteAt':  null }")
    List<ChatRoom> findByParticipant(ObjectId userId);
}
