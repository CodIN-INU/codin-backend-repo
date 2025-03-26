package inu.codin.codin.infra.redis.service;


import inu.codin.codin.domain.post.domain.hits.service.HitsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {
    /**
     * Redis 기반 관리 Service
     * Redis 작업 실패시 DB 기반으로 직접 처리
     * 장애 복구를 대비한 보완 로직 추가
     */
    private final RedisTemplate<String, String> redisTemplate;
    private final HitsService hitsService;


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

    // Top N 게시물 조회
    public Map<String, Double> getTopNPosts(int N) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd/HH");

        Map<String, Double> result = new HashMap<>();
        for (int i = 0; i < 24; i++) {
            String redisKey = now.minusHours(i).format(formatter);
            Set<ZSetOperations.TypedTuple<String>> members = redisTemplate.opsForZSet().reverseRangeWithScores(redisKey, 0, - 1);
            if (members != null) {
                for (ZSetOperations.TypedTuple<String> member :members){
                    String postId = member.getValue();
                    Double score = member.getScore();
                    result.put(postId, score);
                }
            }
        }

        return result.entrySet().stream()
                .sorted((e1, e2) -> {
                    int scoreComparison = Double.compare(e2.getValue(), e1.getValue());
                    if (scoreComparison != 0) {
                        return scoreComparison;
                    }
                    return Integer.compare(hitsService.getHitsCount(new ObjectId(e2.getKey())), hitsService.getHitsCount(new ObjectId(e1.getKey())));
                })
                .limit(N).collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement) -> existing, // Merge function (not needed here)
                        LinkedHashMap::new // Use LinkedHashMap to preserve the order
                ));
    }

    public void applyBestScore(int score, ObjectId id){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd/HH");

        String redisKey;
        for (int i=0; i<24; i++){
            redisKey = now.minusHours(i).format(formatter);
            Double scoreOfBest = redisTemplate.opsForZSet().score(redisKey, id.toString());
            if (scoreOfBest != null){
                redisTemplate.opsForZSet().incrementScore(redisKey, id.toString(), score);
                return;
            }
        }
        redisKey = now.format(DateTimeFormatter.ofPattern("yyyyMMdd/HH"));
        redisTemplate.opsForZSet().add(redisKey, id.toString(), score);
    }
}
