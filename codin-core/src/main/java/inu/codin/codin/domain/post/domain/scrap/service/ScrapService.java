package inu.codin.codin.domain.post.domain.scrap.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.post.domain.like.exception.LikeRemoveFailException;
import inu.codin.codin.domain.post.domain.scrap.entity.ScrapEntity;
import inu.codin.codin.domain.post.domain.scrap.exception.ScrapCreateFailException;
import inu.codin.codin.domain.post.domain.scrap.exception.ScrapRemoveFailException;
import inu.codin.codin.domain.post.domain.scrap.repository.ScrapRepository;
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
public class ScrapService {
    private final ScrapRepository scrapRepository;
    private final PostRepository postRepository;

    private final RedisService redisService;
    private final RedisHealthChecker redisHealthChecker;

    public void toggleScrap(String id) {
        ObjectId postId = new ObjectId(id);
        postRepository.findByIdAndNotDeleted(postId)
                .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다."));

        ObjectId userId = SecurityUtils.getCurrentUserId();

        // 이미 스크랩한 게시물인지 확인
        boolean alreadyScrapped = scrapRepository.existsByPostIdAndUserId(postId, userId);

        if (alreadyScrapped) {
            removeScrap(postId, userId);
        } else {
            addScrap(postId, userId);
        }
    }

    private void addScrap(ObjectId postId, ObjectId userId) {
        // 중복 스크랩 방지
        if (scrapRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new ScrapCreateFailException("이미 스크랩한 게시물입니다.");
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

    private void removeScrap(ObjectId postId, ObjectId userId) {
        // 스크랩하지 않은 게시물이라면 오류 처리
        if (!scrapRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new ScrapRemoveFailException("스크랩한 적이 없는 게시물입니다.");
        }

        if (redisHealthChecker.isRedisAvailable()) {
            redisService.removeScrap(postId, userId);
        }

        scrapRepository.deleteByPostIdAndUserId(postId, userId);
    }

    public int getScrapCount(ObjectId postId) {
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