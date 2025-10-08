package inu.codin.codinticketingapi.domain.ticketing.redis;

import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingErrorCode;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingException;
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

    private static final String AVAILABLE_TICKETS_KEY_PREFIX = "{event:%d}:available";

    // 이벤트 생성 시 티켓 번호 목록을 초기화하는 메서드
    public void initializeTickets(Long eventId, int totalTickets) {
        String key = generateKey(eventId);
        eventRedisTemplate.delete(key);

        for (int i = 1; i <= totalTickets; i++) {
            eventRedisTemplate.opsForZSet().add(key, String.valueOf(i), i);
        }
    }

    public void updateTickets(Long eventId, int totalStock, int prevStock) {
        String key = generateKey(eventId);
        Long remainStock = eventRedisTemplate.opsForZSet().size(key);

        if (remainStock == null) {
            throw new TicketingException(TicketingErrorCode.STOCK_NOT_FOUND);
        }

        if (remainStock < totalStock) {
            increaseStock(key, prevStock, totalStock);

            return;
        }

        if (remainStock > totalStock) {
            decreaseStock(key, prevStock, totalStock);
        }
    }

    // 티켓 번호 하나를 가져오는 메서드
    public Integer getTicket(Long eventId) {
        String key = generateKey(eventId);
        ZSetOperations.TypedTuple<String> ticketNumber = eventRedisTemplate.opsForZSet().popMin(key);

        if (ticketNumber == null || ticketNumber.getValue() == null || ticketNumber.getValue().equals("-1")) {

            throw new TicketingException(TicketingErrorCode.SOLD_OUT);
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

    private void increaseStock(String key, int prevStock, int totalStock) {
        for (int i = prevStock + 1; i <= totalStock; i++) {
            eventRedisTemplate.opsForZSet().add(key, String.valueOf(i), i);
        }
    }

    private void decreaseStock(String key, int prevStock, int totalStock) {
        eventRedisTemplate.opsForZSet().removeRangeByScore(key, totalStock + 1, prevStock);
    }
}
