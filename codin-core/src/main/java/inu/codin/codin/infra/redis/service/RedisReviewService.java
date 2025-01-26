package inu.codin.codin.infra.redis.service;

import inu.codin.codin.domain.lecture.dto.Emotion;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RedisReviewService {

    private static final String REVIEW_KEY = "review:lectures:";
    private final RedisTemplate<String, String> redisTemplate;

    public void addReview(String lectureId, double starRating, ObjectId userId){
        String redisKey = REVIEW_KEY + lectureId;
        redisTemplate.opsForZSet().add(redisKey, String.valueOf(userId), starRating);
    }

    public double getAveOfRating(String lectureId) {
        String redisKey = REVIEW_KEY + lectureId;
        Set<ZSetOperations.TypedTuple<String>> ratingWithScores =
                redisTemplate.opsForZSet().rangeWithScores(redisKey, 0, -1);
        if (ratingWithScores == null || ratingWithScores.isEmpty()) return 0.0;

        double totalScore = ratingWithScores.stream()
                        .mapToDouble(rating -> rating.getScore() != null? rating.getScore():0.0).sum();
        long count = ratingWithScores.size();
        return count > 0 ? totalScore / (double) count : 0.0;
    }

    public Emotion getEmotionRating(String lectureId){
        String redisKey = REVIEW_KEY + lectureId;
        double total = getParticipants(lectureId);
        if (total == 0) return Emotion.builder().hard(0).ok(0).best(0).build();
        return Emotion.builder()
                .hard(getPercentOfRating(redisKey, 0.25, 2.0, total))
                .ok(getPercentOfRating(redisKey, 2.25, 4.0, total))
                .best(getPercentOfRating(redisKey, 4.25, 5.0, total))
                .build();
    }

    private double getPercentOfRating(String redisKey, double min, double max, double total){
        return (Objects.requireNonNull(redisTemplate.opsForZSet().rangeByScore(redisKey, min, max)).size() / total) * 100;
    }

    public long getParticipants(String lectureId){
        String redisKey = REVIEW_KEY + lectureId;
        return Objects.requireNonNull(redisTemplate.opsForZSet().rangeWithScores(redisKey, 0, -1)).size();
    }
}
