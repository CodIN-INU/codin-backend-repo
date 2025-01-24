package inu.codin.codin.infra.redis.service;


import inu.codin.codin.domain.like.entity.LikeType;
import inu.codin.codin.domain.like.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisLikeService {
    /**
     * Redis 기반 Like 관리 Service
     */
    private final RedisTemplate<String, String> redisTemplate;
    private final LikeRepository likeRepository;

    private static final String LIKE_KEY=":likes:";

    //Like
    public void addLike(String entityType, ObjectId entityId, ObjectId userId) {
        String redisKey = entityType + LIKE_KEY + entityId;
        redisTemplate.opsForSet().add(redisKey, String.valueOf(userId));
    }

    public void removeLike(String entityType, ObjectId entityId, ObjectId userId) {
        String redisKey = entityType + LIKE_KEY + entityId;
        redisTemplate.opsForSet().remove(redisKey, String.valueOf(userId));
    }

    public int getLikeCount(String entityType, ObjectId entityId) {
        String redisKey = entityType + LIKE_KEY + entityId;
        Long count = redisTemplate.opsForSet().size(redisKey);
        return count != null ? count.intValue() : 0;
    }

    public Set<String> getLikedUsers(String entityType, String entityId) {
        String redisKey = entityType + LIKE_KEY + entityId;
        return redisTemplate.opsForSet().members(redisKey);
    }

    public boolean isPostLiked(ObjectId postId, ObjectId userId){
        String redisKey = LikeType.POST + LIKE_KEY + postId.toString();
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(redisKey, userId.toString()));
    }
    public boolean isCommentLiked(ObjectId commentId, ObjectId userId){
        String redisKey = LikeType.COMMENT + LIKE_KEY + commentId.toString();
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(redisKey, userId.toString()));
    }
    public boolean isReplyLiked(ObjectId replyId, ObjectId userId){
        String redisKey = LikeType.REPLY + LIKE_KEY + replyId.toString();
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(redisKey, userId.toString()));
    }

    public void recoverRedisFromDB() {
        log.info("Redis 복구 요청 - DB의 좋아요 데이터를 기반으로 복구 시작");
        likeRepository.findAll().forEach(like -> {
            addLike(like.getLikeType().name(), like.getLikeTypeId(), like.getUserId());
            log.info("Redis에 좋아요 복구 - likeType: {}, likeId: {}, userId: {}", like.getLikeType(), like.getLikeTypeId(), like.getUserId());
        });
    }
}
