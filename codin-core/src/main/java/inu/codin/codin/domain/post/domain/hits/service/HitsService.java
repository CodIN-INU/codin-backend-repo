package inu.codin.codin.domain.post.domain.hits.service;

import inu.codin.codin.infra.redis.service.RedisHitsService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HitsService {

    private final RedisHitsService redisHitsService;

    public void addHits(ObjectId postId, ObjectId userId){
        redisHitsService.addHits(postId,userId);
    }

    public boolean validateHits(ObjectId postId, ObjectId userId) {
        return redisHitsService.validateHits(postId,userId);
    }

    public int getHitsCount(ObjectId postId) {
        return redisHitsService.getHitsCount(postId);
    }

}
