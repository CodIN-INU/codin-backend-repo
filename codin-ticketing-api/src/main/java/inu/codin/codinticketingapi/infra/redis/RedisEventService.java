package inu.codin.codinticketingapi.infra.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisEventService {

    private final RedisTemplate<String, String> redisTemplate;

    private final Duration EXTRA_PADDING = Duration.ofHours(2);

    // 전부 예제 코드 완성 아님 ㅎㅎ..
    /** Redis Event 데이터 초기화 */
    public void initializeEvent(Long eventId, int quantity, LocalDateTime eventEnd) {
        // Lua 스크립트?

        String key = getTicketsKey(eventId);
        redisTemplate.delete(key);

        List<String> tickets = IntStream.rangeClosed(1, quantity)
                .mapToObj(String::valueOf)
                .toList();
        redisTemplate.opsForList().rightPushAll(key, tickets);

        LocalDateTime expireAt = eventEnd.plus(EXTRA_PADDING);
        Duration ttl = Duration.between(LocalDateTime.now(), expireAt);
        if (!ttl.isNegative() && !ttl.isZero()) {
            redisTemplate.expire(key, ttl);
        }
    }

    public int allocateFromPreAllocation(Long eventId) {
        String listKey = getTicketsKey(eventId);
        String num = redisTemplate.opsForList().rightPop(listKey);
        if (num == null) {
            return -1;  // 매진
        }
        return Integer.parseInt(num);
    }

    /** 이벤트 삭제 시 Redis 데이터 삭제 */
    public void deleteEvent(Long eventId) {
        String key = getTicketsKey(eventId);
        redisTemplate.delete(key);
    }

    private String getTicketsKey(Long eventId) {
        return "event:" + eventId + ":tickets";
    }
}
