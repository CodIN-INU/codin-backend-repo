package inu.codin.codin.domain.post.domain.like.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.post.domain.comment.repository.CommentRepository;
import inu.codin.codin.domain.post.domain.like.dto.LikeRequestDto;
import inu.codin.codin.domain.post.domain.like.entity.LikeEntity;
import inu.codin.codin.domain.post.domain.like.entity.LikeType;
import inu.codin.codin.domain.post.domain.like.exception.LikeCreateFailException;
import inu.codin.codin.domain.post.domain.like.repository.LikeRepository;
import inu.codin.codin.domain.post.domain.reply.repository.ReplyCommentRepository;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.infra.redis.RedisHealthChecker;
import inu.codin.codin.infra.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

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

    public String toggleLike(LikeRequestDto likeRequestDto) {
        ObjectId likeId = new ObjectId(likeRequestDto.getId());
        ObjectId userId = SecurityUtils.getCurrentUserId();
        isEntityNotDeleted(likeRequestDto); // 해당 entity가 삭제되었는지 확인

        // 이미 좋아요를 눌렀으면 취소, 그렇지 않으면 추가
        LikeEntity like = likeRepository.findByLikeTypeAndLikeTypeIdAndUserId(likeRequestDto.getLikeType(), likeId, userId);

        if (like != null && like.getDeletedAt() == null) {
            removeLike(like);
            return "좋아요가 삭제되었습니다.";
        }
        addLike(likeRequestDto.getLikeType(), likeId, userId);
        return "좋아요가 추가되었습니다.";
    }

    public void addLike(LikeType likeType,ObjectId likeId, ObjectId userId) {
        LikeEntity like = likeRepository.findByLikeTypeAndLikeTypeIdAndUserId(likeType, likeId, userId);

        if (like != null){
            if (like.getDeletedAt() != null) { //삭제된 상태라면 다시 좋아요 만들기
                like.recreatedAt();
                like.restore();
                likeRepository.save(like);
            } else throw new LikeCreateFailException("이미 좋아요가 눌러진 상태입니다.");
        } else { //좋아요 내역이 없으면 새로 생성
            if (redisHealthChecker.isRedisAvailable()) {
                redisService.addLike(likeType.name(), likeId, userId);
            }
            likeRepository.save(LikeEntity.builder()
                    .likeType(likeType)
                    .likeTypeId(likeId)
                    .userId(userId)
                    .build());
            if (likeType == LikeType.POST)
                redisService.applyBestScore(1, likeId);
        }
    }

    public void removeLike(LikeEntity like) {
        if (redisHealthChecker.isRedisAvailable()) {
            redisService.removeLike(like.getLikeType().name(), like.get_id(), like.getUserId());
        }
        like.delete();
        likeRepository.save(like);
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