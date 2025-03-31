package inu.codin.codin.infra.redis.scheduler;

import inu.codin.codin.domain.lecture.domain.review.entity.ReviewEntity;
import inu.codin.codin.domain.lecture.domain.review.repository.ReviewRepository;
import inu.codin.codin.domain.lecture.domain.review.service.ReviewService;
import inu.codin.codin.infra.redis.config.RedisHealthChecker;
import inu.codin.codin.infra.redis.service.RedisBestService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SyncScheduler {

    private final RedisBestService redisBestService;
    private final RedisHealthChecker redisHealthChecker;
    private final ReviewRepository reviewRepository;
    private final ReviewService reviewService;

    @Async
    @Scheduled(fixedRate = 43200000) // 12시간 마다 실행
    public void syncLikes() {
        if (!redisHealthChecker.isRedisAvailable()) {
            log.warn("Redis 비활성화 상태, 동기화 작업 중지");
            return;
        }
//        log.info(" 동기화 작업 시작");
//        syncEntityLikes("POST", postRepository);
//        syncEntityLikes("COMMENT", commentRepository);
//        syncEntityLikes("REPLY", replyCommentRepository);
//        syncEntityLikes("REVIEW", reviewRepository);
//        log.info(" 동기화 작업 완료");
    }
    @Async
    @PostConstruct
    @Scheduled(cron = "0 0 * * * ?") // 1시간 마다 실행
    public void getTop3BestPosts() {
        if (redisHealthChecker.isRedisAvailable()) {
            Map<String, Double> posts = redisBestService.delicatedBestsScheduler(3);
            redisBestService.resetBests(posts);
            posts.forEach((key, value) -> redisBestService.saveBests(key, value.intValue()));
        }
    }

    @PostConstruct
    public void recoverReviews(){
        List<ReviewEntity> reviewEntityList = reviewRepository.findAll();
        reviewEntityList.forEach(
                review -> reviewService.updateRating(review.getLectureId())
        );
    }


}