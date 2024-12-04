package inu.codin.codin.infra.redis;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.domain.post.domain.comment.entity.CommentEntity;
import inu.codin.codin.domain.post.domain.reply.entity.ReplyCommentEntity;
import inu.codin.codin.domain.post.domain.comment.repository.CommentRepository;
import inu.codin.codin.domain.post.domain.reply.repository.ReplyCommentRepository;
import inu.codin.codin.domain.post.domain.like.entity.LikeEntity;
import inu.codin.codin.domain.post.domain.like.repository.LikeRepository;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.domain.like.entity.LikeType;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.post.domain.scrap.entity.ScrapEntity;
import inu.codin.codin.domain.post.domain.scrap.repository.ScrapRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class SyncScheduler {

    private final RedisService redisService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReplyCommentRepository replyCommentRepository;
    private final LikeRepository likeRepository;
    private final ScrapRepository scrapRepository;
    private final RedisHealthChecker redisHealthChecker;

    @Scheduled(fixedRate = 43200000) // 12시간 마다 실행
    public void syncLikes() {
        if (!redisHealthChecker.isRedisAvailable()) {
            log.warn("Redis 비활성화 상태, 동기화 작업 중지");
            return;
        }
        log.info(" 동기화 작업 시작");
        syncEntityLikes("post", postRepository);
        syncEntityLikes("comment", commentRepository);
        syncEntityLikes("reply", replyCommentRepository);
        syncPostScraps();
        log.info(" 동기화 작업 완료");
    }

    private <T> void syncEntityLikes(String entityType, MongoRepository<T, ObjectId> repository) {
        Set<String> redisKeys = redisService.getKeys(entityType+ ":likes:*");
        if (redisKeys == null || redisKeys.isEmpty()) {
            return;
        }
        LikeType likeType = LikeType.valueOf(entityType);

        for (String redisKey : redisKeys) {
            String likeTypeId = redisKey.replace(entityType + ":likes:", "");
            Set<String> likedUsers = redisService.getLikedUsers(entityType, likeTypeId);
            ObjectId likeId = new ObjectId(likeTypeId);

            // (좋아요 삭제) MongoDB에서 Redis에 없는 사용자 삭제
            List<LikeEntity> dbLikes = likeRepository.findByLikeTypeAndLikeTypeId(likeType, likeId);
            for (LikeEntity dbLike : dbLikes) {
                if (!likedUsers.contains(dbLike.getUserId().toString())) {
                    log.info("[MongoDB] 좋아요 삭제: UserID={}, EntityID={}", dbLike.getUserId(), likeId);
                    likeRepository.delete(dbLike);
                }
            }

            // (좋아요 추가) Redis에는 있지만 MongoDB에 없는 사용자 추가
            for (String id : likedUsers) {
                ObjectId userId = new ObjectId(id);
                if (!likeRepository.existsByLikeTypeAndLikeTypeIdAndUserId(likeType, likeId, userId)) {
                    log.info("[MongoDB] 좋아요 추가: UserID={}, EntityID={}", userId, likeId);
                    LikeEntity dbLike = LikeEntity.builder()
                            .likeType(likeType)
                            .likeTypeId(likeId)
                            .userId(userId)
                            .build();
                    likeRepository.save(dbLike);
                }
            }

            // (count 업데이트) Redis 사용자 수로 엔티티의 likeCount 업데이트
            int likeCount = likedUsers.size();
            if (repository instanceof PostRepository postRepo) {
                PostEntity post = postRepo.findByIdAndNotDeleted(likeId).orElse(null);
                if (post != null && post.getLikeCount() != likeCount) {
                    log.info("PostEntity 좋아요 수 업데이트: EntityID={}, Count={}", likeTypeId, likeCount);
                    post.updateLikeCount(likeCount);
                    postRepo.save(post);
                }
            } else if (repository instanceof CommentRepository commentRepo) {
                CommentEntity comment = commentRepo.findByIdAndNotDeleted(likeId).orElse(null);
                if (comment != null && comment.getLikeCount() != likeCount) {
                    log.info("CommentEntity 좋아요 수 업데이트: EntityID={}, Count={}", likeTypeId, likeCount);
                    comment.updateLikeCount(likeCount);
                    commentRepo.save(comment);
                }
            } else if (repository instanceof ReplyCommentRepository replyRepo) {
                ReplyCommentEntity reply = replyRepo.findByIdAndNotDeleted(likeId).orElse(null);
                if (reply != null && reply.getLikeCount() != likeCount) {
                    log.info("ReplyEntity 좋아요 수 업데이트: EntityID={}, Count={}", likeTypeId, likeCount);
                    reply.updateLikeCount(likeCount);
                    replyRepo.save(reply);
                }
            }
        }
    }

    public void syncPostScraps() {

        Set<String> redisKeys = redisService.getKeys("post:scraps:*");
        if (redisKeys == null || redisKeys.isEmpty()) {
            return;
        }

        for (String redisKey : redisKeys) {
            String postId = redisKey.replace("post:scraps:", "");
            Set<String> redisScrappedUsers = redisService.getLikedUsers("post", postId);
            ObjectId id = new ObjectId(postId);

            // MongoDB의 스크랩 데이터 가져오기
            List<ScrapEntity> dbScraps = scrapRepository.findByPostId(id);
            Set<String> dbScrappedUsers = dbScraps.stream()
                    .map(ScrapEntity::getUserId)
                    .map(ObjectId::toString)
                    .collect(Collectors.toSet());

            // (스크랩 삭제) MongoDB에 있지만 Redis에 없는 사용자 삭제
            for (ScrapEntity dbScrap : dbScraps) {
                if (!redisScrappedUsers.contains(dbScrap.getUserId().toString())) {
                    log.info("[MongoDB] 스크랩 삭제: UserID={}, PostID={}", dbScrap.getUserId(), id);
                    scrapRepository.delete(dbScrap);
                }
            }

            // (스크랩 추가) Redis에 있지만 MongoDB에 없는 사용자 추가
            for (String redisUser : redisScrappedUsers) {
                if (!dbScrappedUsers.contains(redisUser)) {
                    log.info("[MongoDB] 스크랩 추가: UserID={}, PostID={}", redisUser, id);
                    ScrapEntity dbScrap = ScrapEntity.builder()
                            .postId(id)
                            .userId(new ObjectId(redisUser))
                            .build();
                    scrapRepository.save(dbScrap);
                }
            }

            // Redis 사용자 수로 PostEntity의 scrapCount 업데이트
            int redisScrapCount = redisScrappedUsers.size();
            PostEntity post = postRepository.findByIdAndNotDeleted(id)
                    .orElseThrow(() -> new NotFoundException("게시물을 찾을 수 없습니다."));
            if (post.getScrapCount() != redisScrapCount) {
                log.info("PostEntity 스크랩 수 업데이트: PostID={}, Count={}", id, redisScrapCount);
                post.updateScrapCount(redisScrapCount);
                postRepository.save(post);
            }
        }
    }
}