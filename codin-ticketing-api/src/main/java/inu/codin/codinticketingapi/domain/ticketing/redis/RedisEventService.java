package inu.codin.codinticketingapi.domain.ticketing.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisEventService {
    private final RedisTemplate<String, String> eventRedisTemplate;

    private static final int SOLD_OUT = -1;
    private static final String AVAILABLE_TICKETS_KEY_PREFIX = "{event:%d}:available";

    // 이벤트 생성 시 티켓 번호 목록을 초기화하는 메서드
    public void initializeTickets(Long eventId, int totalTickets) {
        String key = generateKey(eventId);
        eventRedisTemplate.delete(key);

        for (int i = 1; i <= totalTickets; i++) {
            eventRedisTemplate.opsForZSet().add(key, String.valueOf(i), i);
        }
    }

    // 티켓 번호 하나를 가져오는 메서드
    public Integer getTicket(Long eventId) {
        String key = generateKey(eventId);
        ZSetOperations.TypedTuple<String> ticketNumber = eventRedisTemplate.opsForZSet().popMin(key);

        if (ticketNumber == null || ticketNumber.getValue() == null) {
            System.out.println("null");
            return SOLD_OUT;
        }

        return Integer.parseInt(ticketNumber.getValue());
    }

    // 취소된 티켓 번호를 다시 반환하는 메서드
    public void returnTicket(Long eventId, int ticketNumber) {
        String key = generateKey(eventId);

        eventRedisTemplate.opsForZSet().add(key, String.valueOf(ticketNumber), ticketNumber);
    }

    public void deleteTickets(Long eventId) {
        String key = generateKey(eventId);

        eventRedisTemplate.delete(key);
    }

    private String generateKey(long eventId) {
        return String.format(AVAILABLE_TICKETS_KEY_PREFIX, eventId);
    }

}
