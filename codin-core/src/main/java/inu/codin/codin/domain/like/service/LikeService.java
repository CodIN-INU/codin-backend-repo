package inu.codin.codin.domain.like.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.like.dto.LikeRequestDto;
import inu.codin.codin.domain.like.dto.LikeResponseType;
import inu.codin.codin.domain.like.entity.LikeEntity;
import inu.codin.codin.domain.like.entity.LikeType;
import inu.codin.codin.domain.like.repository.LikeRepository;
import inu.codin.codin.domain.post.domain.comment.repository.CommentRepository;
import inu.codin.codin.domain.post.domain.reply.repository.ReplyCommentRepository;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.infra.redis.config.RedisHealthChecker;
import inu.codin.codin.infra.redis.service.RedisBestService;
import inu.codin.codin.infra.redis.service.RedisLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeService {
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReplyCommentRepository replyCommentRepository;

    private final RedisLikeService redisLikeService;
    private final RedisBestService redisBestService;
    private final RedisHealthChecker redisHealthChecker;


    public LikeResponseType toggleLike(LikeRequestDto likeRequestDto) {
        String likeId = likeRequestDto.getId();
        ObjectId userId = SecurityUtils.getCurrentUserId();
        isEntityNotDeleted(likeRequestDto); // 해당 entity가 삭제되었는지 확인

        // 이미 좋아요를 눌렀으면 취소, 그렇지 않으면 추가
        Optional<LikeEntity> like = likeRepository.findByLikeTypeAndLikeTypeIdAndUserId(likeRequestDto.getLikeType(), likeId, userId);
        return controlLike(likeRequestDto, like, likeId, userId);
    }

    private LikeResponseType controlLike(LikeRequestDto likeRequestDto, Optional<LikeEntity> like, String likeId, ObjectId userId) {
        if (like.isPresent()){
            if (like.get().getDeletedAt() == null) {
                removeLike(like.get());
                return LikeResponseType.REMOVE;
            } else {
                restoreLike(like.get()); //좋아요가 존재하는데 삭제된 상태
                return LikeResponseType.RECOVER;
            }
        } else {
            addLike(likeRequestDto.getLikeType(), likeId, userId);
            return LikeResponseType.ADD;
        }
    }

    /*
        총 좋아요 개수는 Redis에, 누가 눌렀는지는 DB에 저장
     */
    public void addLike(LikeType likeType, String likeId, ObjectId userId){
        if (redisHealthChecker.isRedisAvailable()) {
            redisLikeService.addLike(likeType.name(), likeId);
            log.info("Redis에 좋아요 추가 - likeType: {}, likeId: {}, userId: {}", likeType, likeId, userId);
        }

        likeRepository.save(LikeEntity.builder()
                .likeType(likeType)
                .likeTypeId(likeId)
                .userId(userId)
                .build());
        if (likeType == LikeType.POST) {
            redisBestService.applyBestScore(1, new ObjectId(likeId));
            log.info("Redis에 Best Score 적용 - postId: {}", likeId);
        }
    }

    public void restoreLike(LikeEntity like) {
        ifRedisAvailableAddLike(like.getLikeType(), like.getLikeTypeId());

        like.recreatedAt();
        like.restore();
        likeRepository.save(like);
        log.info("좋아요 복구 완료 - likeId: {}, userId: {}", like.get_id(), like.getUserId());

    }

    private void ifRedisAvailableAddLike(LikeType likeType, String likeId) {
        if (redisHealthChecker.isRedisAvailable()) {
            redisLikeService.addLike(likeType.name(), likeId);
            log.info("Redis에 좋아요 추가 - likeType: {}, likeId: {}", likeType, likeId);
        }
    }

    public void removeLike(LikeEntity like) {
        if (redisHealthChecker.isRedisAvailable()) {
            redisLikeService.removeLike(like.getLikeType().name(), like.getLikeTypeId());
            log.info("Redis에서 좋아요 삭제 - likeType: {}, likeId: {}, userId: {}", like.getLikeType(), like.getLikeTypeId(), like.getUserId());
        }
        like.delete();
        likeRepository.save(like);
        log.info("좋아요 삭제 완료 - likeId: {}, userId: {}", like.get_id(), like.getUserId());
    }

    public int getLikeCount(LikeType entityType, String entityId) {
        //Redis가 사용 가능하면 Redis 시도
        Object redisResult = null;
        if (redisHealthChecker.isRedisAvailable()) {
            redisResult = redisLikeService.getLikeCount(entityType.name(), entityId);
            if (redisResult != null)
                return Integer.parseInt(String.valueOf(redisResult));
        }

        //Redis가 꺼져 있거나 cache가 없을 경우 -> DB 조회
        int likeCount = likeRepository.countByLikeTypeAndLikeTypeIdAndDeletedAtIsNull(entityType, entityId);
        recoveryLike(entityType, entityId, likeCount);
        return likeCount;
    }

    @Async
    protected void recoveryLike(LikeType entityType, String entityId, int likeCount) {
        redisLikeService.recoveryLike(entityType.name(), entityId, likeCount);
    }

    public boolean isLiked(LikeType likeType, String likeTypeId, ObjectId userId){
        return likeRepository.existsByLikeTypeAndLikeTypeIdAndUserIdAndDeletedAtIsNull(likeType, likeTypeId, userId);
    }

    public boolean isLiked(LikeType likeType, String likeTypeId, String userId){
        return likeRepository.existsByLikeTypeAndLikeTypeIdAndUserIdAndDeletedAtIsNull(likeType, likeTypeId, new ObjectId(userId));
    }

    private void isEntityNotDeleted(LikeRequestDto likeRequestDto){
        LikeType likeType = likeRequestDto.getLikeType();
        if (likeType.equals(LikeType.LECTURE) || likeType.equals(LikeType.REVIEW)){
            String likeTypeId = likeRequestDto.getId();
         } else {
            ObjectId id = new ObjectId(likeRequestDto.getId());
            switch(likeType){
                case POST -> postRepository.findByIdAndNotDeleted(id)
                        .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다."));
                case REPLY -> replyCommentRepository.findByIdAndNotDeleted(id)
                        .orElseThrow(() -> new NotFoundException("대댓글을 찾을 수 없습니다."));
                case COMMENT -> commentRepository.findByIdAndNotDeleted(id)
                        .orElseThrow(() -> new NotFoundException("댓글을 찾을 수 없습니다."));
            }
        }

    }


}