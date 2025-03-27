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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisBestService {
    /**
     * Redis Best 게시글 관리
     * Best 게시글은 어떠한 게시글이 관심을 받았을 때부터 ~ 24시간 동안 게시글에 점수가 매겨짐
     * Score {좋아요, 댓글, 대댓글 : 1 / 스크랩 : 2}
     * 24시간 실시간 반영으로 3위까지 나타냄
     */
    private final RedisTemplate<String, String> redisTemplate;
    private final HitsService hitsService;

    /**
     * best 게시글을 반환하는 순간으로부터 24시간 동안의 가장 score가 높은 게시글 N개를 반환
     * @param N 순위
     * @return N개의 Key : postId, Value : score 의 score 기준 내림차순 Map
     */
    public Map<String, Double> getTopNPosts(int N) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd/HH");

        //24시간 동안의 게시글들의 score들을 모으기
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
                    //1. 점수 기준 내림차순
                    int scoreComparison = Double.compare(e2.getValue(), e1.getValue());

                    // 2. 점수가 동일하면 조회수 기준 내림차순 정렬
                    return scoreComparison != 0 ? scoreComparison :
                            Integer.compare(hitsService.getHitsCount(new ObjectId(e2.getKey())),
                                    hitsService.getHitsCount(new ObjectId(e1.getKey())));
                })
                .limit(N).collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    /**
     * 24시간 내에 해당 게시글의 score가 존재하면 점수 반영
     * 만약 새로운 게시글이라면 현재 시간대로 새로 생성하여 게시글 점수 저장
     * @param score 게시글에 더할 점수
     * @param postId 게시글 _id
     */
    public void applyBestScore(int score, ObjectId postId){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd/HH");

        String redisKey;
        //이전 24시간대에서 해당 게시글의 score를 찾기
        for (int i=0; i<24; i++){
            redisKey = now.minusHours(i).format(formatter);
            if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
                Double scoreOfBest = redisTemplate.opsForZSet().score(redisKey, postId.toString());
                if (scoreOfBest != null) { //score가 존재하면 업데이트
                    redisTemplate.opsForZSet().incrementScore(redisKey, postId.toString(), score);
                    return;
                }
            }
        }
        //score가 존재하지 않으면 현재 시간대로 새로 생성
        redisKey = now.format(formatter);
        redisTemplate.expire(redisKey, 1, TimeUnit.DAYS);
        redisTemplate.opsForZSet().add(redisKey, postId.toString(), score);
    }
}
