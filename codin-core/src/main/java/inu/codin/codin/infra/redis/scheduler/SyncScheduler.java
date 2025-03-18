package inu.codin.codin.infra.redis.scheduler;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.domain.lecture.domain.review.entity.ReviewEntity;
import inu.codin.codin.domain.lecture.domain.review.repository.ReviewRepository;
import inu.codin.codin.domain.lecture.repository.LectureRepository;
import inu.codin.codin.domain.post.domain.best.BestEntity;
import inu.codin.codin.domain.post.domain.best.BestRepository;
import inu.codin.codin.domain.post.domain.comment.entity.CommentEntity;
import inu.codin.codin.domain.post.domain.hits.entity.HitsEntity;
import inu.codin.codin.domain.post.domain.hits.repository.HitsRepository;
import inu.codin.codin.domain.post.domain.reply.entity.ReplyCommentEntity;
import inu.codin.codin.domain.post.domain.comment.repository.CommentRepository;
import inu.codin.codin.domain.post.domain.reply.repository.ReplyCommentRepository;
import inu.codin.codin.domain.like.entity.LikeEntity;
import inu.codin.codin.domain.like.repository.LikeRepository;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.like.entity.LikeType;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.scrap.entity.ScrapEntity;
import inu.codin.codin.domain.scrap.repository.ScrapRepository;
import inu.codin.codin.infra.redis.config.RedisHealthChecker;
import inu.codin.codin.infra.redis.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class SyncScheduler {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReplyCommentRepository replyCommentRepository;
    private final ReviewRepository reviewRepository;

    private final LikeRepository likeRepository;
    private final ScrapRepository scrapRepository;
    private final HitsRepository hitsRepository;
    private final BestRepository bestRepository;
    private final LectureRepository lectureRepository;

    private final RedisService redisService;
    private final RedisLikeService redisLikeService;
    private final RedisHitsService redisHitsService;
    private final RedisScrapService redisScrapService;
    private final RedisReviewService redisReviewService;
    private final RedisHealthChecker redisHealthChecker;

    @Async
    @Scheduled(fixedRate = 43200000) // 12시간 마다 실행
    public void syncLikes() {
        if (!redisHealthChecker.isRedisAvailable()) {
            log.warn("Redis 비활성화 상태, 동기화 작업 중지");
            return;
        }
        log.info(" 동기화 작업 시작");
        syncEntityLikes("POST", postRepository);
        syncEntityLikes("COMMENT", commentRepository);
        syncEntityLikes("REPLY", replyCommentRepository);
        syncEntityLikes("REVIEW", reviewRepository);
        syncPostScraps();
        syncPostHits();
        syncReviews();
        log.info(" 동기화 작업 완료");
    }

    private void syncReviews() {
        List<ReviewEntity> dbReviews = reviewRepository.findAllByDeletedAtIsNull();
        Set<String> redisKeys = redisService.getKeys("review:lectures:*").stream()
                .map(key -> key.replace("review:lectures:", ""))
                .collect(Collectors.toSet());

        //Map< lectureId, 해당되는 리뷰Entity 리스트 >
        Map<String, List<ReviewEntity>> dbReviewMap = dbReviews.stream()
                .collect(Collectors.groupingBy(review -> review.getLectureId().toString()));

        // Redis에 없는 리뷰 복구
        dbReviewMap.forEach((lectureId, reviews) -> {
            if (!redisKeys.contains(lectureId)) {
                // Redis에 강의 자체가 없으면 모든 리뷰 추가
                reviews.forEach(review -> redisReviewService.addReview(
                        lectureId, review.getStarRating(), review.getUserId()));
            } else {
                // Redis에 강의는 있지만 특정 유저 리뷰가 없는 경우 추가
                Set<String> redisUsers = redisReviewService.getReviewUsers(lectureId);
                reviews.stream()
                        .filter(review -> !redisUsers.contains(review.getUserId().toString()))
                        .forEach(review -> {
                            log.info("[Redis] 리뷰 추가: UserID={}, LectureID={}", review.getUserId(), lectureId);
                            redisReviewService.addReview(lectureId, review.getStarRating(), review.getUserId());
                        });
            }
        });

        // DB에 없는 리뷰 삭제
        for (String lectureId : redisKeys) {
            Set<String> redisUsers = redisReviewService.getReviewUsers(lectureId);
            Set<String> dbUsers = dbReviewMap.getOrDefault(lectureId, List.of()).stream()
                    .map(review -> review.getUserId().toString())
                    .collect(Collectors.toSet());

            redisUsers.stream().filter(userId -> !dbUsers.contains(userId))
                    .forEach(userId -> {
                        log.info("[Redis] 리뷰 삭제: UserID={}, LectureID={}", userId, lectureId);
                        redisReviewService.removeReview(lectureId, userId);
                    });
        }

        // 강의 평점 업데이트
        dbReviewMap.keySet().forEach(lectureId -> {
            lectureRepository.findById(new ObjectId(lectureId)).ifPresent(lectureEntity -> {
                lectureEntity.updateReviewRating(
                        redisReviewService.getAveOfRating(lectureId),
                        redisReviewService.getParticipants(lectureId),
                        redisReviewService.getEmotionRating(lectureId)
                );
                lectureRepository.save(lectureEntity);
            });
        });
    }

    private <T> void syncEntityLikes(String entityType, MongoRepository<T, ObjectId> repository) {
        Set<String> redisKeys = redisService.getKeys(entityType+ ":likes:*")
                .stream().map(redisKey -> redisKey.replace(entityType+ ":likes:", ""))
                .collect(Collectors.toSet());
        if (redisKeys.isEmpty()) return;

        LikeType likeType = LikeType.valueOf(entityType);

        // 좋아요 대상 _id 와 좋아요 entity 리스트
        List<LikeEntity> dbLikes = likeRepository.findByLikeTypeAndDeletedAtIsNull(likeType);
        Map<String, List<LikeEntity>> dbLikeMap = dbLikes.stream()
                .collect(Collectors.groupingBy(likeEntity -> likeEntity.getLikeTypeId().toString()));

        //DB에 있지만 Redis에 없는 좋아요 복구
        dbLikeMap.forEach((entityId, likes) -> {
            if (!redisKeys.contains(entityId))
                likes.forEach(like -> redisLikeService.addLike(entityType, like.getLikeTypeId(), like.getUserId()));
        });

        //Redis에 있지만 DB에 없는 좋아요 삭제
        for (String likeTypeId : redisKeys) {
            Set<String> redisUsers = redisLikeService.getLikedUsers(entityType, likeTypeId);
            Set<String> dbUsers = dbLikeMap.getOrDefault(likeTypeId, List.of()).stream()
                    .map(like -> like.getUserId().toString())
                    .collect(Collectors.toSet());

            ObjectId entityTypeId = new ObjectId(likeTypeId);

            redisUsers.stream().filter(userId -> !dbUsers.contains(userId))
                    .forEach(userId -> {
                        log.info("[Redis] 좋아요 삭제: UserID={}, LikeID={}", userId, likeTypeId);
                        redisLikeService.removeLike(entityType, entityTypeId, new ObjectId(userId));
                    });

            // likeCount 업데이트
            int likeCount = redisLikeService.getLikeCount(entityType, new ObjectId(likeTypeId));
            if (repository instanceof PostRepository postRepo) {
                PostEntity post = postRepo.findByIdAndNotDeleted(entityTypeId).orElse(null);
                if (post != null && post.getLikeCount() != likeCount) {
                    log.info("PostEntity 좋아요 수 업데이트: EntityID={}, Count={}", entityTypeId, likeCount);
                    post.updateLikeCount(likeCount);
                    postRepo.save(post);
                }
            } else if (repository instanceof CommentRepository commentRepo) {
                CommentEntity comment = commentRepo.findByIdAndNotDeleted(entityTypeId).orElse(null);
                if (comment != null && comment.getLikeCount() != likeCount) {
                    log.info("CommentEntity 좋아요 수 업데이트: EntityID={}, Count={}", entityTypeId, likeCount);
                    comment.updateLikeCount(likeCount);
                    commentRepo.save(comment);
                }
            } else if (repository instanceof ReplyCommentRepository replyRepo) {
                ReplyCommentEntity reply = replyRepo.findByIdAndNotDeleted(entityTypeId).orElse(null);
                if (reply != null && reply.getLikeCount() != likeCount) {
                    log.info("ReplyEntity 좋아요 수 업데이트: EntityID={}, Count={}", entityTypeId, likeCount);
                    reply.updateLikeCount(likeCount);
                    replyRepo.save(reply);
                }
            } else if (repository instanceof ReviewRepository reviewRepo) {
                ReviewEntity review = reviewRepo.findBy_idAndDeletedAtIsNull(entityTypeId).orElse(null);
                if (review != null && review.getLikeCount() != likeCount) {
                    log.info("ReviewEntity 좋아요 수 업데이트: EntityID={}, Count={}", entityTypeId, likeCount);
                    review.updateLikeCount(likeCount);
                    reviewRepo.save(review);
                }
            }
        }
    }


    @Scheduled(fixedRate = 43200000)
    public void syncPostScraps() {
        Set<String> redisKeys = redisService.getKeys("post:scraps:*")
                .stream().map(redisKey -> redisKey.replace("post:scraps:", ""))
                .collect(Collectors.toSet());
        if (redisKeys.isEmpty()) return;

        //Post의 _id에 해당되는 Scrap Entity 리스트
        List<ScrapEntity> dbScraps = scrapRepository.findAllByDeletedAtIsNull();
        Map<String, List<ScrapEntity>> dbScrapMap = dbScraps.stream()
                .collect(Collectors.groupingBy(scrapEntity -> scrapEntity.getPostId().toString()));

        //DB에 있지만 Redis에 없는 Scrap 복구
        dbScrapMap.forEach((postId, scraps) -> {
            if (!redisKeys.contains(postId))
                scraps.forEach(scrap -> redisScrapService.addScrap(new ObjectId(postId), scrap.getUserId()));
        });

        //DB에 없지만 Redis에 있는 Scrap 삭제
        for (String postId : redisKeys){
            Set<String> redisUsers = redisScrapService.getScrapedUsers(new ObjectId(postId));
            Set<String> dbUsers = dbScrapMap.getOrDefault(postId, List.of()).stream()
                    .map(scrap -> scrap.getUserId().toString())
                    .collect(Collectors.toSet());

            redisUsers.stream().filter(userId -> !dbUsers.contains(userId))
                    .forEach(userId -> {
                        log.info("[MongoDB] 스크랩 삭제: UserID={}, PostID={}", userId, postId);
                        redisScrapService.removeScrap(new ObjectId(postId), new ObjectId(userId));
                    });

            //scrap 개수 업데이트
            int scarpCount = redisScrapService.getScrapCount(new ObjectId(postId));
            PostEntity post = postRepository.findByIdAndNotDeleted(new ObjectId(postId))
                    .orElse(null);
            if (post != null && post.getScrapCount() != scarpCount) {
                log.info("PostEntity 스크랩 수 업데이트: PostID={}, Count={}", postId, scarpCount);
                post.updateScrapCount(scarpCount);
                postRepository.save(post);
            }
        }
    }

    @Scheduled(fixedRate = 43200000)
    public void syncPostHits(){
        Set<String> redisKeys = redisService.getKeys("post:hits:*")
                .stream().map(redisKey -> redisKey.replace("post:hits:", ""))
                .collect(Collectors.toSet());
        if (redisKeys.isEmpty()) return;

        // Post _id에 해당하는 Hits Entity 리스트
        List<HitsEntity> dbHits = hitsRepository.findAll();
        Map<String, List<HitsEntity>> dbHitsMap = dbHits.stream()
                .collect(Collectors.groupingBy(hitsEntity -> hitsEntity.getPostId().toString()));

        //DB에 있지만 redis에 없는 조회수 복구
        dbHitsMap.forEach((postId, hits)-> {
            if (!redisKeys.contains(postId))
                hits.forEach(hit -> redisHitsService.addHits(new ObjectId(postId), hit.getUserId()));
        });

        //DB에 없지만 redis에 있는 조회수 삭제
        for (String postId: redisKeys){
            Set<String> redisUsers = redisHitsService.getHitsUser(new ObjectId(postId));
            Set<String> dbUsers = dbHitsMap.getOrDefault(postId, List.of())
                    .stream().map(hit -> hit.getUserId().toString())
                    .collect(Collectors.toSet());

            redisUsers.stream().filter(userId -> !dbUsers.contains(userId))
                    .forEach(userId -> {
                        log.info("[Redis] 조회수 삭제 : UserId = {}, PostId = {}", userId, postId);
                        redisHitsService.removeHits(new ObjectId(postId), userId);
                    });

            //조회수 count 업데이트
            int hitCount = redisHitsService.getHitsCount(new ObjectId(postId));
            PostEntity post = postRepository.findByIdAndNotDeleted(new ObjectId(postId))
                    .orElse(null);
            if (post != null && post.getHitCount() != hitCount){
                log.info("PostEntity 조회수 업데이트: PostID={}, Count={}", postId, hitCount);
                post.updateHitCount(hitCount);
                postRepository.save(post);
            }
        }
    }

    @Scheduled(fixedRate = 43200000) // 12시간 마다 실행
    public void getTop3BestPosts() {
        Map<String, Double> posts = redisService.getTopNPosts(3);
        posts.entrySet().stream()
                .peek(post -> {
                    BestEntity bestPost = bestRepository.findByPostId(new ObjectId(post.getKey()));
                    if (bestPost == null) {
                        bestRepository.save(BestEntity.builder()
                                .postId(new ObjectId(post.getKey()))
                                .score(post.getValue().intValue())
                                .build());
                    }
                }
        );
    }

}