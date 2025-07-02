package inu.codin.codin.domain.chat.chatting.repository;

import inu.codin.codin.domain.chat.chatting.entity.Chatting;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class CustomChattingRepository {

    private final MongoTemplate mongoTemplate;

    public CustomChattingRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Chatting findMostRecentByChatRoomId(ObjectId chatRoomId) {
        Query query = new Query(Criteria.where("chatRoomId").is(chatRoomId))
                .with(Sort.by(Sort.Direction.DESC, "createdAt"))
                .limit(1);
        return mongoTemplate.findOne(query, Chatting.class);
    }

}
