package inu.codin.codin.domain.post.domain.like.service;

import inu.codin.codin.domain.post.domain.like.entity.LikeEntity;
import inu.codin.codin.domain.post.domain.like.exception.LikeCreateFailException;
import inu.codin.codin.domain.post.domain.like.exception.LikeRemoveFailException;
import inu.codin.codin.domain.post.domain.like.entity.LikeType;
import inu.codin.codin.domain.post.domain.like.exception.InvalidLikeTypeException;
import inu.codin.codin.domain.post.domain.like.repository.LikeRepository;
import inu.codin.codin.infra.redis.RedisHealthChecker;
import inu.codin.codin.infra.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeService {

    private final RedisService redisService;
    private final LikeRepository likeRepository;
    private final RedisHealthChecker redisHealthChecker;

    public void addLike(LikeType entityType, String entityId, String userId) {
        //유효한 entityType 검증
        validateLikeType(entityType);
        // 중복 좋아요 검증
        if (likeRepository.existsByEntityTypeAndEntityIdAndUserId(entityType, entityId, userId)) {
            throw new LikeCreateFailException("이미 좋아요를 누른 상태입니다.");
        }

        if (redisHealthChecker.isRedisAvailable()) {
            redisService.addLike(entityType.name(), entityId, userId);
        }
        LikeEntity like = LikeEntity.builder()
                .entityType(entityType)
                .entityId(entityId)
                .userId(userId)
                .build();
        likeRepository.save(like);
        }

    public void removeLike(LikeType entityType, String entityId, String userId) {
        //유효한 entityType 검증
        validateLikeType(entityType);

        // 없는 좋아요 방지
        if (!likeRepository.existsByEntityTypeAndEntityIdAndUserId(entityType, entityId, userId)) {
            throw new LikeRemoveFailException(" 좋아요를 누른적이 없습니다.");
        }
        if (redisHealthChecker.isRedisAvailable()) {
            redisService.removeLike(entityType.name(), entityId, userId);
        }
        likeRepository.deleteByEntityTypeAndEntityIdAndUserId(entityType, entityId, userId);
    }

    public int getLikeCount(LikeType entityType, String entityId) {
        //유효한 entityType 검증
        validateLikeType(entityType);

        if (redisHealthChecker.isRedisAvailable()) {
            return redisService.getLikeCount(entityType.name(), entityId);
        }
        Long count = likeRepository.countByEntityTypeAndEntityId(entityType, entityId);
        return (int) Math.max(0, count);
    }

    public void recoverRedisFromDB() {
        likeRepository.findAll().forEach(like -> {
            redisService.addLike(like.getEntityType().name(), like.getEntityId(), like.getUserId());
        });
    }

    //EnumSet.allOf 를 사용해 Enum 값 집합 처리
    private void validateLikeType(LikeType entityType) {
        if (entityType == null || !EnumSet.allOf(LikeType.class).contains(entityType)) {
            throw new InvalidLikeTypeException("유효하지 않은 LikeType입니다: " + entityType);
        }
    }

}