package inu.codin.codinticketingapi.domain.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisEventService {

    private final RedisTemplate<String, String> redisTemplate;

    /** 이벤트 초기 데이터를 Redis에 세팅 */
    public void setEventData(Long eventId, int quantity) {
        // 남은 수량 초기화
        String key = buildRemainKey(eventId);
        redisTemplate.opsForValue().set(key, String.valueOf(quantity));
    }

    /** 이벤트 삭제 시 Redis 데이터 삭제 */
    public void deleteEventData(Long eventId) {
        String key = buildRemainKey(eventId);
        redisTemplate.delete(key);
    }

    private String buildRemainKey(Long eventId) {
        return "event:" + eventId + ":remain";
    }
}
