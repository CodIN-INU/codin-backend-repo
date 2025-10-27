package inu.codin.codin.infra.redis.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisHitsService {
    /**
     * Redis 기반 Hits(조회수) 관리 Service
     */
    private final RedisTemplate<String, String> redisTemplate;

    private static final String HITS_KEY = "post:hits:";

    /**
     * 조회수 추가 (post:hits:{postId} - 조회수)
     * @param postId 게시글 _id
     */
    public void addHits(ObjectId postId){
        String redisKey = HITS_KEY + postId.toString();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey)))
            redisTemplate.opsForValue().increment(redisKey);
        else {
            redisTemplate.opsForValue().set(redisKey, String.valueOf(1));
        }
        redisTemplate.expire(redisKey, 1, TimeUnit.DAYS);
    }

    /**
     * 게시글 조회수 반환
     * @param postId 게시글 _id
     * @return int : 게시글 조회수
     */
    public Object getHitsCount(ObjectId postId){
        String redisKey = HITS_KEY + postId.toString();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))){
            redisTemplate.expire(redisKey, 1, TimeUnit.DAYS);
            return redisTemplate.opsForValue().get(redisKey);
        }
        else return null;
    }

    /**
     * hits Collection 기반으로 Cache 복구
     * @param postId 게시글 _id
     * @param hits 해당 게시글의 조회수
     */
    public void recoveryHits(ObjectId postId, int hits){
        String redisKey = HITS_KEY + postId.toString();
        redisTemplate.expire(redisKey, 1, TimeUnit.DAYS);
        redisTemplate.opsForValue().set(redisKey, String.valueOf(hits));
    }
}
