package inu.codin.codin.infra.redis.service;


import inu.codin.codin.domain.like.entity.LikeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisLikeService {
    /**
     * Redis 기반 Like 관리 Service
     */
    private final RedisTemplate<String, String> redisTemplate;

    private static final String LIKE_KEY=":likes:";

    //Like
    public void addLike(String entityType, ObjectId entityId) {
        String redisKey = entityType + LIKE_KEY + entityId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey)))
            redisTemplate.opsForValue().increment(redisKey);
        else {
            redisTemplate.opsForValue().set(redisKey, String.valueOf(1));
        }
        redisTemplate.expire(redisKey, 1, TimeUnit.DAYS);
    }

    public void removeLike(String entityType, ObjectId entityId) {
        String redisKey = entityType + LIKE_KEY + entityId;
        redisTemplate.opsForValue().decrement(redisKey, 1);
    }

    public Object getLikeCount(String entityType, ObjectId entityId) {
        String redisKey = entityType + LIKE_KEY + entityId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))){
            redisTemplate.expire(redisKey, 1, TimeUnit.DAYS);
            return redisTemplate.opsForValue().get(redisKey);
        } else return null;

    }

    public void recoveryLike(LikeType entityType, ObjectId entityId, int likeCount) {
        String redisKey = entityType + LIKE_KEY + entityId;
        redisTemplate.expire(redisKey, 1, TimeUnit.DAYS);
        redisTemplate.opsForValue().set(redisKey, String.valueOf(likeCount));
    }
}
