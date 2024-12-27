package inu.codin.codin.domain.post.domain.like.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.post.domain.comment.repository.CommentRepository;
import inu.codin.codin.domain.post.domain.like.dto.LikeRequestDto;
import inu.codin.codin.domain.post.domain.like.entity.LikeEntity;
import inu.codin.codin.domain.post.domain.like.exception.LikeCreateFailException;
import inu.codin.codin.domain.post.domain.like.exception.LikeRemoveFailException;
import inu.codin.codin.domain.post.domain.like.entity.LikeType;
import inu.codin.codin.domain.post.domain.like.repository.LikeRepository;
import inu.codin.codin.domain.post.domain.reply.repository.ReplyCommentRepository;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.infra.redis.RedisHealthChecker;
import inu.codin.codin.infra.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.validation.ObjectError;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeService {
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReplyCommentRepository replyCommentRepository;

    private final RedisService redisService;
    private final RedisHealthChecker redisHealthChecker;

    public void toggleLike(LikeRequestDto likeRequestDto) {
        ObjectId likeId = new ObjectId(likeRequestDto.getId());
        ObjectId userId = SecurityUtils.getCurrentUserId();
        isEntityNotDeleted(likeRequestDto); // 해당 entity가 삭제되었는지 확인

        // 이미 좋아요를 눌렀으면 취소, 그렇지 않으면 추가
        boolean alreadyLiked = likeRepository.existsByLikeTypeAndLikeTypeIdAndUserId(likeRequestDto.getLikeType(), likeId, userId);

        if (alreadyLiked) {
            removeLike(likeRequestDto.getLikeType(), likeId, userId);
        } else {
            addLike(likeRequestDto.getLikeType(), likeId, userId);
        }
    }

    public void addLike(LikeType likeType,ObjectId likeId, ObjectId userId) {
        // 중복 좋아요 검증
        if (likeRepository.existsByLikeTypeAndLikeTypeIdAndUserId(likeType, likeId, userId)) {
            throw new LikeCreateFailException("이미 좋아요를 누른 상태입니다.");
        }
        if (redisHealthChecker.isRedisAvailable()) {
            redisService.addLike(likeType.name(), likeId, userId);
        }
        LikeEntity like = LikeEntity.builder()
                .likeType(likeType)
                .likeTypeId(likeId)
                .userId(userId)
                .build();
        likeRepository.save(like);
    }

    public void removeLike(LikeType likeType,ObjectId likeId, ObjectId userId) {
        // 없는 좋아요 방지
        if (!likeRepository.existsByLikeTypeAndLikeTypeIdAndUserId(likeType, likeId, userId)) {
            throw new LikeRemoveFailException(" 좋아요를 누른적이 없습니다.");
        }
        if (redisHealthChecker.isRedisAvailable()) {
            redisService.removeLike(likeType.name(), likeId, userId);
        }
        likeRepository.deleteByLikeTypeAndLikeTypeIdAndUserId(likeType, likeId, userId);
    }

    public int getLikeCount(LikeType entityType, ObjectId entityId) {
        if (redisHealthChecker.isRedisAvailable()) {
            return redisService.getLikeCount(entityType.name(), entityId);
        }
        long count = likeRepository.countByLikeTypeAndLikeTypeId(entityType, entityId);
        return (int) Math.max(0, count);
    }

    public void recoverRedisFromDB() {
        likeRepository.findAll().forEach(like -> {
            redisService.addLike(like.getLikeType().name(), like.getLikeTypeId(), like.getUserId());
        });
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

        }
    }

}