package inu.codin.codin.infra.redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisAnonService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String ANON_KEY = "anon:";
    private static final String COUNTER_KEY = "anon_counter:";

    public Integer getAnonNumber(String postId, String userId){
        String anonKey = ANON_KEY + postId + ":" + userId;
        String counterKey = COUNTER_KEY + postId;

        Object existingAnonNum = redisTemplate.opsForValue().get(anonKey);
        if (existingAnonNum != null){
            return Integer.parseInt(existingAnonNum.toString());
        }

        Object counterAnonNum = redisTemplate.opsForValue().get(counterKey);
        if (counterAnonNum == null){
            redisTemplate.opsForValue().set(counterKey, 0); //카운터 1부터 시작
        }

        redisTemplate.opsForValue().increment(counterKey);
        int counter = Integer.parseInt(redisTemplate.opsForValue().get(counterKey).toString());
        redisTemplate.opsForValue().set(anonKey, counter); //유저에게 익명 번호 설정
        return counter;
    }

    public void setWriter(String postId, String userId){
        String anonKey = ANON_KEY + postId + ":" + userId;
        redisTemplate.opsForValue().set(anonKey, 0);
    }
}
