package inu.codin.codin.infra.redis.service;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisReviewService {

    private static final String REVIEW_KEY = "review:lectures:";
    private final RedisTemplate<String, String> redisTemplate;

    public void addReview(String lectureCode, double starRating, ObjectId userId){
        String redisKey = REVIEW_KEY + lectureCode;
        redisTemplate.opsForZSet().add(redisKey, String.valueOf(userId), starRating);
    }

}
