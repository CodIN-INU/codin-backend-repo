package inu.codin.codin.infra.redis.service;


import inu.codin.codin.domain.scrap.repository.ScrapRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisScrapService {
    /**
     * Redis 기반 Scrap 관리 Service
     **/
    private final RedisTemplate<String, String> redisTemplate;
    private final ScrapRepository scrapRepository;

    private static final String SCRAP_KEY = "post:scraps:";

    //Scrap
    public void addScrap(ObjectId postId, ObjectId userId) {
        String redisKey = SCRAP_KEY + postId.toString();
        redisTemplate.opsForSet().add(redisKey, userId.toString());
    }

    public void removeScrap(ObjectId postId, ObjectId userId) {
        String redisKey = SCRAP_KEY + postId.toString();
        redisTemplate.opsForSet().remove(redisKey, userId.toString());
    }

    public int getScrapCount(ObjectId postId) {
        String redisKey = SCRAP_KEY + postId.toString();
        Long scrapCount = redisTemplate.opsForSet().size(redisKey);
        return scrapCount != null ? scrapCount.intValue() : 0;
    }

    public boolean isPostScraped(ObjectId postId, ObjectId userId){
        String redisKey = SCRAP_KEY + postId.toString();
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(redisKey, userId.toString()));
    }

    public void recoverRedisFromDB() {
        log.info("Redis 복구 요청 - DB의 스크랩 데이터를 기반으로 복구 시작");

        scrapRepository.findAll().forEach(scrap -> {
            addScrap(scrap.getPostId(), scrap.getUserId());
            log.info("Redis에 스크랩 복구 - postId: {}, userId: {}", scrap.getPostId(), scrap.getUserId());
        });

        log.info("Redis 복구 완료");
    }
}
