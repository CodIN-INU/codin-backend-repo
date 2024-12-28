package inu.codin.codin.domain.chat.chatting.repository;

import inu.codin.codin.domain.chat.chatting.entity.Chatting;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class CustomChattingRepository {

    private final ReactiveMongoTemplate mongoTemplate;

    public CustomChattingRepository(ReactiveMongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Mono<Chatting> findMostRecentByChatRoomId(ObjectId chatRoomId) {
        Query query = new Query(Criteria.where("chatRoomId").is(chatRoomId))
                .with(Sort.by(Sort.Direction.DESC, "createdAt"))
                .limit(1);
        return mongoTemplate.findOne(query, Chatting.class);
    }

}
