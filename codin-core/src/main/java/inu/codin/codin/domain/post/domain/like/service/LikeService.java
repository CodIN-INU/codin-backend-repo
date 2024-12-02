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

    public void addLike(LikeRequestDto likeRequestDto) {
        String userId = SecurityUtils.getCurrentUserId();
        isEntityNotDeleted(likeRequestDto); //해당 entity가 삭제되었는지 확인
        // 중복 좋아요 검증
        if (likeRepository.existsByLikeTypeAndLikeTypeIdAndUserId(likeRequestDto.getLikeType(), likeRequestDto.getId(), userId)) {
            throw new LikeCreateFailException("이미 좋아요를 누른 상태입니다.");
        }
        if (redisHealthChecker.isRedisAvailable()) {
            redisService.addLike(likeRequestDto.getLikeType().name(), likeRequestDto.getId(), userId);
        }
        LikeEntity like = LikeEntity.builder()
                .likeType(likeRequestDto.getLikeType())
                .likeTypeId(likeRequestDto.getId())
                .userId(userId)
                .build();
        likeRepository.save(like);
        }

    public void removeLike(LikeRequestDto likeRequestDto) {
        String userId = SecurityUtils.getCurrentUserId();
        isEntityNotDeleted(likeRequestDto);
        // 없는 좋아요 방지
        if (!likeRepository.existsByLikeTypeAndLikeTypeIdAndUserId(likeRequestDto.getLikeType(), likeRequestDto.getId(), userId)) {
            throw new LikeRemoveFailException(" 좋아요를 누른적이 없습니다.");
        }
        if (redisHealthChecker.isRedisAvailable()) {
            redisService.removeLike(likeRequestDto.getLikeType().name(), likeRequestDto.getId(), userId);
        }
        likeRepository.deleteByLikeTypeAndLikeTypeIdAndUserId(likeRequestDto.getLikeType(), likeRequestDto.getId(), userId);
    }

    public int getLikeCount(LikeType entityType, String entityId) {
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
        String id = likeRequestDto.getId();
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