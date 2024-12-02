package inu.codin.codin.domain.post.domain.scrap;

import inu.codin.codin.domain.post.domain.like.exception.LikeRemoveFailException;
import inu.codin.codin.domain.post.domain.scrap.exception.ScrapCreateFailException;
import inu.codin.codin.infra.redis.RedisHealthChecker;
import inu.codin.codin.infra.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScrapService {

    private final RedisService redisService;
    private final ScrapRepository scrapRepository;
    private final RedisHealthChecker redisHealthChecker;

    public void addScrap(String postId, String userId) {
        if (scrapRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new ScrapCreateFailException("이미 스크랩 한 상태 입니다.");
        }

        if (redisHealthChecker.isRedisAvailable()) {
            redisService.addScrap(postId, userId);
        }
        ScrapEntity scrap = ScrapEntity.builder()
                .postId(postId)
                .userId(userId)
                .build();
        scrapRepository.save(scrap);
    }

    public void removeScrap(String postId, String userId) {
        if (!scrapRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new LikeRemoveFailException("스크랩한 적이 없는 게시물입니다.");
        }

        if (redisHealthChecker.isRedisAvailable()) {
            redisService.removeScrap(postId, userId);
        }
        scrapRepository.deleteByPostIdAndUserId(postId, userId);
    }

    public int getScrapCount(String postId) {
        if (redisHealthChecker.isRedisAvailable()) {
            return redisService.getScrapCount(postId);
        }
        long count = scrapRepository.countByPostId(postId);
        return (int) Math.max(0, count);
    }

    public void recoverRedisFromDB() {
        scrapRepository.findAll().forEach(scrap -> {
            redisService.addScrap(scrap.getPostId(), scrap.getUserId());
        });
    }
}