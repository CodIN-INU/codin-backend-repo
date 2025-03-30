package inu.codin.codin.domain.post.domain.hits.service;

import inu.codin.codin.domain.post.domain.hits.entity.HitsEntity;
import inu.codin.codin.domain.post.domain.hits.repository.HitsRepository;
import inu.codin.codin.infra.redis.config.RedisHealthChecker;
import inu.codin.codin.infra.redis.service.RedisHitsService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Look Aside
 * - Cache miss -> DB 조회 및 Cache 등록
 * Write Back
 * - SyncScheduler 동기화
 */
@Service
@RequiredArgsConstructor
public class HitsService {

    private final RedisHitsService redisHitsService;
    private final RedisHealthChecker redisHealthChecker;

    private final HitsRepository hitsRepository;

    /**
     * 게시글 조회수 추가
     * Cache 업데이트 후 DB 업데이트
     * @param postId 게시글 _id
     * @param userId 유저 _id
     */
    public void addHits(ObjectId postId, ObjectId userId){
        if (redisHealthChecker.isRedisAvailable())
            redisHitsService.addHits(postId);
        HitsEntity hitsEntity = HitsEntity.builder()
                .postId(postId).userId(userId).build();
        hitsRepository.save(hitsEntity);
    }

    /**
     * 게시글 조회 여부 판단
     * @param postId 게시글 _id
     * @param userId 유저 _id
     * @return true : 게시글 조회 유 , false : 게시글 조회 무
     */
    public boolean validateHits(ObjectId postId, ObjectId userId) {
        return hitsRepository.existsByPostIdAndUserId(postId, userId);
    }

    /**
     * 게시글 조회수 반환
     * null : Cache miss로 @Async로 Cache 복구 및 DB 조회
     * @param postId 게시글 _id
     * @return 게시글 조회수
     */
    public int getHitsCount(ObjectId postId) {
        Object hits = null;
        if (redisHealthChecker.isRedisAvailable())
            hits = redisHitsService.getHitsCount(postId);
        if (hits == null) {
            recoveryHits(postId);
            return hitsRepository.countAllByPostId(postId);
        }
        else return Integer.parseInt((String)hits);
    }

    /**
     * Cache miss로 인한 DB로부터 Cache 복구
     * @param postId 게시글 _id
     */
    @Async
    protected void recoveryHits(ObjectId postId) {
        int hits = hitsRepository.countAllByPostId(postId);
        redisHitsService.recoveryHits(postId, hits);
    }

}
