package inu.codin.codin.infra.redis;


import inu.codin.codin.domain.post.domain.like.entity.LikeType;
import inu.codin.codin.domain.post.entity.PostEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {
    /**
     * Redis 기반 Like/Scrap 관리 Service
     * Redis 작업 실패시 DB 기반으로 직접 처리
     * 장애 복구를 대비한 보완 로직 추가
     */
    private final RedisTemplate<String, String> redisTemplate;

    private static final String LIKE_KEY=":likes:";
    private static final String SCRAP_KEY = "post:scraps:";
    private static final String HITS_KEY = "post:hits:";


    //post, comment, reply 구분
    public Set<String> getKeys(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys == null || keys.isEmpty()) {
                return Set.of(); // keys가 null이거나 빈 경우 빈 Set 반환
            }
            return keys.stream()
                    .filter(key -> key != null && !key.isEmpty()) // key가 null 또는 빈 문자열이 아닌 경우 필터링
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.warn("Redis 연결 중 오류 발생: {}", e.getMessage());
            return Set.of(); // Redis 예외 발생 시 빈 Set 반환
        }
    }

    //Like
    public void addLike(String entityType, ObjectId entityId, ObjectId userId) {
        String redisKey = entityType + LIKE_KEY + entityId;
        redisTemplate.opsForSet().add(redisKey, String.valueOf(userId));
    }

    public void removeLike(String entityType, ObjectId entityId, ObjectId userId) {
        String redisKey = entityType + LIKE_KEY + entityId;
        redisTemplate.opsForSet().remove(redisKey, String.valueOf(userId));
    }

    public int getLikeCount(String entityType, ObjectId entityId) {
        String redisKey = entityType + LIKE_KEY + entityId;
        Long count = redisTemplate.opsForSet().size(redisKey);
        return count != null ? count.intValue() : 0;
    }

    public Set<String> getLikedUsers(String entityType, String entityId) {
        String redisKey = entityType + LIKE_KEY + entityId;
        return redisTemplate.opsForSet().members(redisKey);
    }

    public boolean isPostLiked(ObjectId postId, ObjectId userId){
        String redisKey = LikeType.POST + LIKE_KEY + postId.toString();
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(redisKey, userId.toString()));
    }

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

    //Hits
    public void addHits(ObjectId postId, ObjectId userId){
        String redisKey = HITS_KEY + postId.toString();
        redisTemplate.opsForSet().add(redisKey, userId.toString());
    }

    public boolean validateHits(ObjectId postId, ObjectId userId){
        String redisKey = HITS_KEY + postId.toString();
        return Boolean.FALSE.equals(redisTemplate.opsForSet().isMember(redisKey, userId.toString())); //없어야 유효성 검증 통과
    }

    public int getHitsCount(ObjectId postId){
        String redisKey = HITS_KEY + postId.toString();
        Long hitsCount = redisTemplate.opsForSet().size(redisKey);
        return hitsCount != null ? hitsCount.intValue() : 0;
    }

    public Set<String> getHitsUser(ObjectId postId) {
        String redisKey = HITS_KEY + postId.toString();
        return redisTemplate.opsForSet().members(redisKey);
    }


    // 점수 업데이트 Sorted Set 사용
    public void updateScore(String postId, double score) {
        redisTemplate.opsForZSet().add("post:ranking", postId, score);
    }

    // Top N 게시물 조회
    public Set<String> getTopNPosts(int N) {
        return redisTemplate.opsForZSet().reverseRange("post:ranking", 0, N - 1);
    }

    public void applyBestScore(int score, PostEntity post){
        LocalDateTime now = LocalDateTime.now();
        int hour = now.toLocalTime().getHour();
        int day = now.toLocalDate().getDayOfMonth();
        String rediskey;
        for (int i=0; i<24; i++){
            if ((hour-i) < 0){ hour = hour+24; day= day-1; }
            rediskey = now.format(DateTimeFormatter.ofPattern("yyyyMM")) + day + "/" + (hour-i);
            Double scoreOfBest = redisTemplate.opsForZSet().score(rediskey, post.get_id().toString());
            if (scoreOfBest != null){
                redisTemplate.opsForZSet().incrementScore(rediskey, post.get_id().toString(), score);
                break;
            }
        }
        rediskey = now.format(DateTimeFormatter.ofPattern("yyyyMMdd/HH"));
        redisTemplate.opsForZSet().add(rediskey, post.get_id().toString(), score);
    }
}
