package inu.codin.codin.domain.post.like;

import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.like.exception.LikeCreateFailException;
import inu.codin.codin.domain.post.like.exception.LikeRemoveFailException;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.infra.redis.RedisHealthChecker;
import inu.codin.codin.infra.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeService {

    private final RedisService redisService;
    private final LikeRepository likeRepository;
    private final RedisHealthChecker redisHealthChecker;

    public void addLike(String entityType, String entityId, String userId) {

        // 중복 좋아요 검증
        if (likeRepository.existsByEntityTypeAndEntityIdAndUserId(entityType, entityId, userId)) {
            throw new LikeCreateFailException("이미 좋아요를 누른 상태입니다.");
        }

        if (redisHealthChecker.isRedisAvailable()) {
            redisService.addLike(entityType, entityId, userId);
        }
        LikeEntity like = LikeEntity.builder()
                .entityType(entityType)
                .entityId(entityId)
                .userId(userId)
                .build();
        likeRepository.save(like);
        }

    public void removeLike(String entityType, String entityId, String userId) {
        // 없는 좋아요 방지
        if (!likeRepository.existsByEntityTypeAndEntityIdAndUserId(entityType, entityId, userId)) {
            throw new LikeRemoveFailException(" 좋아요를 누른적이 없습니다.");
        }
        if (redisHealthChecker.isRedisAvailable()) {
            redisService.removeLike(entityType, entityId, userId);
        }
        likeRepository.deleteByEntityTypeAndEntityIdAndUserId(entityType, entityId, userId);
    }

    public int getLikeCount(String entityType, String entityId) {
        if (redisHealthChecker.isRedisAvailable()) {
            return redisService.getLikeCount(entityType, entityId);
        }
        Long count = likeRepository.countByEntityTypeAndEntityId(entityType, entityId);
        return (int) Math.max(0, count);
    }

    public void recoverRedisFromDB() {
        likeRepository.findAll().forEach(like -> {
            redisService.addLike(like.getEntityType(), like.getEntityId(), like.getUserId());
        });
    }
}