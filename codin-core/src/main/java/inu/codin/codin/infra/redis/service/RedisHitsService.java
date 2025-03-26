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
     * 조회수 추가 (post:hits:{postId} - {userId})
     * @param postId 게시글 _id
     * @param userId 유저 _id
     */
    public void addHits(ObjectId postId, ObjectId userId){
        String redisKey = HITS_KEY + postId.toString();
        redisTemplate.opsForSet().add(redisKey, userId.toString());
        redisTemplate.expire(redisKey, 3, TimeUnit.DAYS);
    }

    /**
     * 조회수 중복 방지를 위한 유저의 게시글 조회 여부
     * @param postId 게시글 _id
     * @param userId 유저 _id
     * @return true : 게시글 조회 유, false : 게시글 조회 무
     */
    public Object validateHits(ObjectId postId, ObjectId userId){
        String redisKey = HITS_KEY + postId.toString();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey)))
            return redisTemplate.opsForSet().isMember(redisKey, userId.toString()); //없어야 유효성 검증 통과
        return null;
    }

    /**
     * 게시글 조회수 반환
     * @param postId 게시글 _id
     * @return int : 게시글 조회수
     */
    public Object getHitsCount(ObjectId postId){
        String redisKey = HITS_KEY + postId.toString();
        Long hitsCount = redisTemplate.opsForSet().size(redisKey);
        if (hitsCount == null) return null;
        return hitsCount.intValue();
    }
}
