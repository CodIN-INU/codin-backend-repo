package inu.codin.codin.infra.redis.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisLikeService {
    /**
     * Redis 기반 Like 관리 Service, TTL = 1DAYS
     */
    private final RedisTemplate<String, String> redisTemplate;

    private static final String LIKE_KEY=":likes:";

    //Like
    public void addLike(String entityType, String entityId) {
        String redisKey = makeRedisKey(entityType, entityId);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey)))
            redisTemplate.opsForValue().increment(redisKey);
        else {
            redisTemplate.opsForValue().set(redisKey, String.valueOf(1));
        }
        redisTemplate.expire(redisKey, 1, TimeUnit.DAYS);
    }

    public void removeLike(String entityType, String entityId) {
        String redisKey = makeRedisKey(entityType, entityId);
        redisTemplate.opsForValue().decrement(redisKey, 1);
    }

    public Object getLikeCount(String entityType, String entityId) {
        String redisKey = makeRedisKey(entityType, entityId);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))){
            redisTemplate.expire(redisKey, 1, TimeUnit.DAYS);
            return redisTemplate.opsForValue().get(redisKey);
        } else return null;

    }

    public void recoveryLike(String entityType, String entityId, int likeCount) {
        String redisKey = makeRedisKey(entityType, entityId);
        redisTemplate.expire(redisKey, 1, TimeUnit.DAYS);
        redisTemplate.opsForValue().set(redisKey, String.valueOf(likeCount));
    }

    private static String makeRedisKey(String entityType, String entityId) {
        return entityType + LIKE_KEY + entityId;
    }

}
