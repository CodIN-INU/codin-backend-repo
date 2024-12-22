package inu.codin.codin.domain.chat.chatting.repository;

import inu.codin.codin.domain.chat.chatting.entity.Chatting;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ChattingRepositoryCustomImpl implements CustomChattingRepository{

    private final MongoTemplate mongoTemplate;
    @Override
    public Mono<Chatting> findRecentMessageByChatRoomId(ObjectId chatRoomId) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("chatRoomId").is(chatRoomId)),
                Aggregation.sort(Sort.by(Sort.Order.desc("createdAt"))),
                Aggregation.limit(1)
        );

        AggregationResults<Chatting> result = mongoTemplate.aggregate(aggregation, "chatting", Chatting.class);

        // Use Mono.justOrEmpty to ensure it returns Mono.empty() when there is no result
        return Mono.justOrEmpty(result.getUniqueMappedResult());
    }

}
