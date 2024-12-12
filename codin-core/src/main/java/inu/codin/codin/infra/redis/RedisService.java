package inu.codin.codin.infra.redis;


import inu.codin.codin.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {
    /**
     * Redis 기반 Like/Scrap 관리 Service
     * Redis 작업 실패시 DB 기반으로 직접 처리
     * 장애 복구를 대비한 보완 로직 추가
     */
    private final RedisTemplate<String, String> redisTemplate;
    private final PostRepository postRepository;

    //post, comment, reply 구분
    public Set<String> getKeys(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys == null || keys.isEmpty()) {
                return Set.of(); // keys가 null이거나 빈 경우 빈 Set 반환
            }
            return keys.stream()
                    .filter(key -> key != null && !key.isEmpty()) // key가 null 또는 빈 문자열이 아닌 경우 필터링
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.warn("Redis 연결 중 오류 발생: {}", e.getMessage());
            return Set.of(); // Redis 예외 발생 시 빈 Set 반환
        }
    }

    public void addLike(String entityType, ObjectId entityId, ObjectId userId) {
        String redisKey = entityType + ":likes:" + entityId;
        redisTemplate.opsForSet().add(redisKey, String.valueOf(userId));
    }

    public void removeLike(String entityType, ObjectId entityId, ObjectId userId) {
        String redisKey = entityType + ":likes:" + entityId;
        redisTemplate.opsForSet().remove(redisKey, String.valueOf(userId));
    }

    public int getLikeCount(String entityType, ObjectId entityId) {
        String redisKey = entityType + ":likes:" + entityId;
        Long count = redisTemplate.opsForSet().size(redisKey);
        return count != null ? count.intValue() : 0;
    }

    public Set<String> getLikedUsers(String entityType, String entityId) {
        String redisKey = entityType + ":likes:" + entityId;
        return redisTemplate.opsForSet().members(redisKey);
    }

    public void addScrap(ObjectId postId, ObjectId userId) {
        String redisKey = "post:scraps:" + postId.toString();
        redisTemplate.opsForSet().add(redisKey, userId.toString());
    }

    public void removeScrap(ObjectId postId, ObjectId userId) {
        String redisKey = "post:scraps:" + postId.toString();
        redisTemplate.opsForSet().remove(redisKey, userId.toString());
    }

    public int getScrapCount(ObjectId postId) {
        String redisKey = "post:scraps:" + postId.toString();
        Long scrapCount = redisTemplate.opsForSet().size(redisKey);
        return scrapCount != null ? scrapCount.intValue() : 0;
    }
}
