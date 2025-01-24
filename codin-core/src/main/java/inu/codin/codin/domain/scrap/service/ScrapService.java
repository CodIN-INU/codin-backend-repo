package inu.codin.codin.domain.scrap.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.scrap.entity.ScrapEntity;
import inu.codin.codin.domain.scrap.exception.ScrapCreateFailException;
import inu.codin.codin.domain.scrap.repository.ScrapRepository;
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

    public String toggleScrap(String id) {
        log.info("스크랩 토글 요청 - postId: {}", id);

        ObjectId postId = new ObjectId(id);
        postRepository.findByIdAndNotDeleted(postId)
                .orElseThrow(() -> {
                    log.warn("스크랩 토글 실패 - 게시글을 찾을 수 없음 - postId: {}", postId);
                    return new NotFoundException("게시글을 찾을 수 없습니다.");
                });

        ObjectId userId = SecurityUtils.getCurrentUserId();

        // 이미 스크랩한 게시물인지 확인
        ScrapEntity scrap =  scrapRepository.findByPostIdAndUserId(postId, userId);

        if (scrap != null && scrap.getDeletedAt() == null) {
            removeScrap(scrap);
            return "스크랩이 취소되었습니다. ";
        }
        addScrap(postId, userId);
        return "스크랩이 추가되었습니다. ";

    }

    private void addScrap(ObjectId postId, ObjectId userId) {
        log.info("스크랩 추가 요청 - postId: {}, userId: {}", postId, userId);

        ScrapEntity scrap =  scrapRepository.findByPostIdAndUserId(postId, userId);

        if (scrap != null){
            if (scrap.getDeletedAt() != null){
                scrap.recreatedAt();
                scrap.restore();
                scrapRepository.save(scrap);
            } else {
                log.warn("스크랩 추가 실패 - 이미 스크랩된 상태 - postId: {}, userId: {}", postId, userId);
                throw new ScrapCreateFailException("이미 스크랩이 된 상태입니다.");
            }
        } else {
            if (redisHealthChecker.isRedisAvailable()) {
                redisService.addScrap(postId, userId);
                log.info("Redis에 스크랩 추가 - postId: {}, userId: {}", postId, userId);
            }
            scrapRepository.save(ScrapEntity.builder()
                    .postId(postId)
                    .userId(userId)
                    .build());
            redisService.applyBestScore(2, postId);
            log.info("Redis에 Best Score 적용 - postId: {}", postId);
        }
    }

    private void removeScrap(ScrapEntity scrap) {
        log.info("스크랩 삭제 요청 - postId: {}, userId: {}", scrap.getPostId(), scrap.getUserId());
        if (redisHealthChecker.isRedisAvailable()) {
            redisService.removeScrap(scrap.getPostId(), scrap.getUserId());
            log.info("Redis에서 스크랩 삭제 - postId: {}, userId: {}", scrap.getPostId(), scrap.getUserId());
        }
        scrap.delete();
        scrapRepository.save(scrap);
        log.info("스크랩 삭제 완료 - postId: {}, userId: {}", scrap.getPostId(), scrap.getUserId());
    }

    public int getScrapCount(ObjectId postId) {
        if (redisHealthChecker.isRedisAvailable()) {
            return redisService.getScrapCount(postId);
        }
        long count = scrapRepository.countByPostIdAndDeletedAtIsNull(postId);
        return (int) Math.max(0, count);
    }

    public void recoverRedisFromDB() {
        log.info("Redis 복구 요청 - DB의 스크랩 데이터를 기반으로 복구 시작");

        scrapRepository.findAll().forEach(scrap -> {
            redisService.addScrap(scrap.getPostId(), scrap.getUserId());
            log.info("Redis에 스크랩 복구 - postId: {}, userId: {}", scrap.getPostId(), scrap.getUserId());
        });

        log.info("Redis 복구 완료");
    }
}