package inu.codin.codin.domain.chat.chatting.repository;

import inu.codin.codin.domain.chat.chatting.entity.Chatting;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChattingRepository extends ReactiveMongoRepository<Chatting, String> {

    @Query("{ '_id': ?0, 'deletedAt': null }")
    Mono<Chatting> findById(ObjectId id);

    Flux<Chatting> findAllByChatRoomIdOrderByCreatedAt(ObjectId id, Pageable pageable);
}
