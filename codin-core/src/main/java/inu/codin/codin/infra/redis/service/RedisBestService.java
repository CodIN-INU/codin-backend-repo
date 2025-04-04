package inu.codin.codin.infra.redis.service;


import inu.codin.codin.domain.post.domain.best.BestEntity;
import inu.codin.codin.domain.post.domain.best.BestRepository;
import inu.codin.codin.domain.post.domain.hits.service.HitsService;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.infra.redis.config.RedisHealthChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    private final RedisHealthChecker redisHealthChecker;

    private final HitsService hitsService;
    private final BestRepository bestRepository;
    private final PostRepository postRepository;
    private final String BEST_KEY = "post:top3";

    /**
     * 이전 1시간 동안의 가장 score가 높은 게시글 N개를 반환, DB 조회가 안된다면 삭제
     * @param N 순위
     * @return N개의 Key : postId, Value : score 의 score 기준 내림차순 Map
     */
    public Map<String, Double> delicatedBestsScheduler(int N) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd/HH");

        //24시간 동안의 게시글들의 score들을 모으기
        Map<String, Double> result = new HashMap<>();
        for (int i = 0; i < 24; i++) {
            String redisKey = now.minusHours(i).format(formatter);
            if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
                Set<ZSetOperations.TypedTuple<String>> members = redisTemplate.opsForZSet().reverseRangeWithScores(redisKey, 0, -1);
                if (members != null) {
                    for (ZSetOperations.TypedTuple<String> member : members) {
                        String postId = member.getValue();
                        Double score = member.getScore();
                        if (!postRepository.existsBy_idAndDeletedAtIsNull(new ObjectId(postId))){
                            redisTemplate.opsForZSet().remove(redisKey, postId);
                            deleteBest(postId);
                            break;
                        }
                        result.put(postId, score);
                    }
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
     * Cache에서 저장 중인 Best 게시글 정보 정렬 및 반환
     * @return Key : PostId (게시글_id) - Value : Score (점수)
     */
    public Map<String, Double> getBests(){
        if (redisHealthChecker.isRedisAvailable()){
            if (Boolean.TRUE.equals(redisTemplate.hasKey(BEST_KEY))){
                Set<ZSetOperations.TypedTuple<String>> members = redisTemplate.opsForZSet().rangeWithScores(BEST_KEY, 0, - 1);
                if (members!=null && !members.isEmpty())
                    return members.stream()
                            .sorted((e1, e2) -> {
                                //1. 점수 기준 내림차순
                                int scoreComparison = Double.compare(Double.parseDouble(String.valueOf(e2.getScore())), Double.parseDouble(String.valueOf(e1.getScore())));

                                // 2. 점수가 동일하면 조회수 기준 내림차순 정렬
                                return scoreComparison != 0 ? scoreComparison :
                                        Integer.compare(hitsService.getHitsCount(new ObjectId(e2.getValue())),
                                                hitsService.getHitsCount(new ObjectId(e1.getValue())));
                            }).collect(Collectors.toMap(
                                    ZSetOperations.TypedTuple::getValue,
                                    ZSetOperations.TypedTuple::getScore,
                                    (existing, replacement) -> existing,
                                    LinkedHashMap::new
                            ));
            } else log.warn("[getBests] Best 게시글이 없습니다.");
        }
        return new HashMap<>();
    }
    /**
     * Cache에 저장된 24시간 데이터 중 해당 게시글의 score가 존재하면 점수 반영
     * 만약 새로운 게시글이라면 현재 시간대로 새로 생성하여 게시글 점수 저장
     * @param score 게시글에 더할 점수
     * @param postId 게시글 _id
     */
    public void applyBestScore(int score, ObjectId postId){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd/HH");

        String redisKey;
        if (redisHealthChecker.isRedisAvailable()) {
            //이전 24시간대에서 해당 게시글의 score를 찾기
            for (int i = 0; i < 24; i++) {
                redisKey = now.minusHours(i).format(formatter);
                if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
                    Double scoreOfBest = redisTemplate.opsForZSet().score(redisKey, postId.toString());
                    if (scoreOfBest != null) { //score가 존재하면 업데이트
                        redisTemplate.opsForZSet().incrementScore(redisKey, postId.toString(), score);
                        updateBests(redisKey, postId.toString());
                        return;
                    }
                }
            }
            //score가 존재하지 않으면 현재 시간대로 새로 생성
            redisKey = now.format(formatter);
            redisTemplate.expire(redisKey, 1, TimeUnit.DAYS); //하루가 지나면 필요없는 데이터
            redisTemplate.opsForZSet().add(redisKey, postId.toString(), score);
            updateBests(redisKey, postId.toString());
        }
    }

    /**
     * 게시글에 점수가 반영 될 때마다 베스트 게시글을 관리하는 ZSet에서 업데이트
     * 베스트 게시글에서 최소 점수보다 같거나 크거나, 베스트 게시글이 3개 이하거나 할 때 베스트 게시글에 포함
     *
     * 만약 포함 후 3개 초과라면
     * 새로 적용된 게시글의 점수와 최소 점수와 같으면 조회수로 판단
     * 상위 3개를 제외한 나머지 삭제 후 DB 저장
     */
    private void updateBests(String redisKey, String postId){
        Double score = redisTemplate.opsForZSet().score(redisKey, postId);
        Set<ZSetOperations.TypedTuple<String>> minEntry = redisTemplate.opsForZSet().rangeWithScores(BEST_KEY, 0, 0);

        if (minEntry!=null && !minEntry.isEmpty()){
            ZSetOperations.TypedTuple<String> minTuple = minEntry.iterator().next(); //최소 score의 Tuple
            Double min = minTuple.getScore();
            Long totalSize = redisTemplate.opsForZSet().size(BEST_KEY);

            //최소 점수보다 같거나 큰 값이거나, 총 베스트 게시글 개수가 3개 미만 이면 포함
            if (score >= min || totalSize < 3)
                redisTemplate.opsForZSet().add(BEST_KEY, postId, score);

            totalSize = redisTemplate.opsForZSet().size(BEST_KEY);
            if (totalSize > 3) { //3개가 넘어가면
                if (score.equals(min)) //만약 최소값끼리 같으면 조회수로 비교
                    checkHits(postId, minTuple.getValue());
                else redisTemplate.opsForZSet().removeRange(BEST_KEY, 0, totalSize-4); //상위 3개를 제외하고 삭제
            }

            Set<ZSetOperations.TypedTuple<String>> members = redisTemplate.opsForZSet().rangeWithScores(BEST_KEY, 0, - 1);
            members.forEach(member -> saveBests(member.getValue(), member.getScore().intValue()));
        } else
            redisTemplate.opsForZSet().add(BEST_KEY, postId, score);
    }

    /**
     * 베스트 게시글에 처음 적용된 게시글과 기존에 있던 최소 점수 게시글의 조회수 비교
     * @param postId 점수가 적용된 게시글 _id
     * @param minPostId 베스트 게시글 중에서 가장 최소 점수를 가진 게시글 _id
     */
    private void checkHits(String postId, String minPostId) {
        if (hitsService.getHitsCount(new ObjectId(minPostId)) > hitsService.getHitsCount(new ObjectId(postId))) {
            redisTemplate.opsForZSet().remove(BEST_KEY, postId);
        } else {
            redisTemplate.opsForZSet().remove(BEST_KEY, minPostId);
        }

    }

    /**
     * 만약 bestEntity에 없다면 저장
     */
    public void saveBests(String postId, int score) {
        boolean existedPost = bestRepository.existsByPostId(new ObjectId(postId));
        if (!existedPost) {
            bestRepository.save(BestEntity.builder()
                    .postId(new ObjectId(postId))
                    .score(score)
                    .build());
        }
    }

    /**
     * 동기화 과정에서 기존에 있던 값을 지우고 새롭게 등록
     * 스케줄러를 통해 매 시마다 전 1시간 동안의 베스트 게시글을 등록
     */
    public void resetBests(Map<String, Double> posts) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(BEST_KEY))) {
            redisTemplate.opsForZSet().removeRange(BEST_KEY, 0 ,-1);
        }
        posts.forEach((key, value) -> redisTemplate.opsForZSet().add(BEST_KEY, key, value));
    }

    /**
     * 만약 post:top3에 포함된 게시글이 삭제되었다면 삭제
     * @param postId
     */
    public void deleteBest(String postId){
        if (Boolean.TRUE.equals(redisTemplate.hasKey(BEST_KEY))){
            redisTemplate.opsForZSet().remove(BEST_KEY, postId);
        }
    }
}
