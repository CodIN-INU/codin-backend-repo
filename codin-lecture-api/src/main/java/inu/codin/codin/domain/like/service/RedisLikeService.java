package inu.codin.codin.domain.like.service;


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

    public Object getLikeCount(String entityType, String entityId) {
        String redisKey = makeRedisKey(entityType, entityId);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))){
            redisTemplate.expire(redisKey, 1, TimeUnit.DAYS);
            return redisTemplate.opsForValue().get(redisKey);
        } else return null;
    }

    private static String makeRedisKey(String entityType, String entityId) {
        return entityType + LIKE_KEY + entityId;
    }

}
