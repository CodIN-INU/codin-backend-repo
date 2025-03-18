package inu.codin.codin.infra.redis.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisHitsService {
    /**
     * Redis 기반 Hits 관리 Service
     */
    private final RedisTemplate<String, String> redisTemplate;

    private static final String HITS_KEY = "post:hits:";

    //Hits
    public void addHits(ObjectId postId, ObjectId userId){
        String redisKey = HITS_KEY + postId.toString();
        redisTemplate.opsForSet().add(redisKey, userId.toString());
    }

    public boolean validateHits(ObjectId postId, ObjectId userId){
        String redisKey = HITS_KEY + postId.toString();
        return Boolean.FALSE.equals(redisTemplate.opsForSet().isMember(redisKey, userId.toString())); //없어야 유효성 검증 통과
    }

    public int getHitsCount(ObjectId postId){
        String redisKey = HITS_KEY + postId.toString();
        Long hitsCount = redisTemplate.opsForSet().size(redisKey);
        return hitsCount != null ? hitsCount.intValue() : 0;
    }

    public Set<String> getHitsUser(ObjectId postId) {
        String redisKey = HITS_KEY + postId.toString();
        return redisTemplate.opsForSet().members(redisKey);
    }

    public void removeHits(ObjectId postId, String userId) {
        String redisKey = HITS_KEY + postId.toString();
        redisTemplate.opsForSet().remove(redisKey, userId);
    }
}
