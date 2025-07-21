package inu.codin.codinticketingsse.sse.consumer;

import inu.codin.codinticketingsse.sse.service.SseService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class SseEventConsumer {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SseService sseService;

    private static final String STREAM_KEY = "event-stock-stream";
    private static final String GROUP_NAME = "event-group";
    private static final String CONSUMER_NAME = "event-consumer";

    @PostConstruct
    public void init() {
        try {
            redisTemplate.opsForStream().createGroup(STREAM_KEY, GROUP_NAME);
            log.info("[RedisStreamConsumer] Redis Consumer 그룹 생성: {}", GROUP_NAME);
        } catch (Exception e) {
            log.warn("[RedisStreamConsumer] Redis Consumer 그룹 에러: {}", e.getMessage());
        }
    }

    public void consumeEventStockStream() {
        StreamOperations<String, Object, Object> streamOps = redisTemplate.opsForStream();

        List<MapRecord<String, Object, Object>> messages = streamOps.read(
                Consumer.from(GROUP_NAME, CONSUMER_NAME),
                StreamReadOptions.empty().count(10).block(Duration.ofSeconds(2)),
                StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed())
        );

        if (messages == null || messages.isEmpty()) return;

        for (MapRecord<String, Object, Object> record : messages) {
            try {
                Map<Object, Object> data = record.getValue();

                Long eventId = Long.parseLong(data.get("eventId").toString());
                Long quantity = Long.parseLong(data.get("quantity").toString());

                log.info("[RedisStreamConsumer] 메시지 수신: eventId={}, quantity={}", eventId, quantity);

                sseService.publishEventStock(eventId, quantity); // SSE 발행

                streamOps.acknowledge(STREAM_KEY, GROUP_NAME, record.getId());

            } catch (Exception e) {
                log.error("[RedisStreamConsumer] 메시지 처리 실패: {}", e.getMessage());
            }
        }
    }
}
