package inu.codin.codin.domain.like.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.lecture.domain.review.repository.ReviewRepository;
import inu.codin.codin.domain.like.entity.LikeEntity;
import inu.codin.codin.domain.like.entity.LikeType;
import inu.codin.codin.domain.like.repository.LikeRepository;
import inu.codin.codin.domain.post.domain.comment.repository.CommentRepository;
import inu.codin.codin.domain.like.dto.LikeRequestDto;
import inu.codin.codin.domain.post.domain.reply.repository.ReplyCommentRepository;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.infra.redis.config.RedisHealthChecker;
import inu.codin.codin.infra.redis.service.RedisLikeService;
import inu.codin.codin.infra.redis.service.RedisBestService;
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
    private final ReviewRepository reviewRepository;

    private final RedisLikeService redisLikeService;
    private final RedisBestService redisBestService;
    private final RedisHealthChecker redisHealthChecker;


    public String toggleLike(LikeRequestDto likeRequestDto) {
        ObjectId likeId = new ObjectId(likeRequestDto.getId());
        ObjectId userId = SecurityUtils.getCurrentUserId();
        isEntityNotDeleted(likeRequestDto); // 해당 entity가 삭제되었는지 확인

        // 이미 좋아요를 눌렀으면 취소, 그렇지 않으면 추가
        Optional<LikeEntity> like = likeRepository.findByLikeTypeAndLikeTypeIdAndUserId(likeRequestDto.getLikeType(), likeId, userId);
        return getResult(likeRequestDto, like, likeId, userId);
    }

    private String getResult(LikeRequestDto likeRequestDto, Optional<LikeEntity> like, ObjectId likeId, ObjectId userId) {
        if (like.isPresent()){
            if (like.get().getDeletedAt() == null) {
                removeLike(like.get());
                return "좋아요가 삭제되었습니다.";
            } else {
                restoreLike(like.get()); //좋아요가 존재하는데 삭제된 상태
                return "좋아요가 복구되었습니다";
            }
        } else {
            addLike(likeRequestDto.getLikeType(), likeId, userId);
            return "좋아요가 추가되었습니다.";
        }
    }

    public void addLike(LikeType likeType, ObjectId likeId, ObjectId userId){
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
            redisBestService.applyBestScore(1, likeId);
            log.info("Redis에 Best Score 적용 - postId: {}", likeId);
        }
    }

    public void restoreLike(LikeEntity like) {
        if (redisHealthChecker.isRedisAvailable()) {
            redisLikeService.addLike(like.getLikeType().name(), like.getLikeTypeId());
            log.info("Redis에 좋아요 추가 - likeType: {}, likeId: {}", like.getLikeType(), like.getLikeTypeId());
        }

        like.recreatedAt();
        like.restore();
        likeRepository.save(like);
        log.info("좋아요 복구 완료 - likeId: {}, userId: {}", like.get_id(), like.getUserId());

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

    public int getLikeCount(LikeType entityType, ObjectId entityId) {
        Object redisResult = null;
        if (redisHealthChecker.isRedisAvailable()) {
            redisResult = redisLikeService.getLikeCount(entityType.name(), entityId);
        }
        if (redisResult == null){
            recoveryLike(entityType, entityId);
            return likeRepository.countByLikeTypeAndLikeTypeIdAndDeletedAtIsNull(entityType, entityId);
        } else return Integer.parseInt(String.valueOf(redisResult));
    }

    @Async
    protected void recoveryLike(LikeType entityType, ObjectId entityId) {
        int likeCount = likeRepository.countAllByLikeTypeAndLikeTypeIdAndDeletedAtIsNull(entityType, entityId);
        redisLikeService.recoveryLike(entityType, entityId, likeCount);
    }

    public boolean isLiked(LikeType likeType, ObjectId likeTypeId, ObjectId userId){
        return likeRepository.existsByLikeTypeAndLikeTypeIdAndUserIdAndDeletedAtIsNull(likeType, likeTypeId, userId);
    }

    private void isEntityNotDeleted(LikeRequestDto likeRequestDto){
        ObjectId id = new ObjectId(likeRequestDto.getId());
        switch(likeRequestDto.getLikeType()){
            case POST -> postRepository.findByIdAndNotDeleted(id)
                    .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다."));
            case REPLY -> replyCommentRepository.findByIdAndNotDeleted(id)
                    .orElseThrow(() -> new NotFoundException("대댓글을 찾을 수 없습니다."));
            case COMMENT -> commentRepository.findByIdAndNotDeleted(id)
                    .orElseThrow(() -> new NotFoundException("댓글을 찾을 수 없습니다."));
            case REVIEW -> reviewRepository.findBy_idAndDeletedAtIsNull(id)
                    .orElseThrow(() ->new NotFoundException("수강 후기를 찾을 수 없습니다"));

        }
    }


}