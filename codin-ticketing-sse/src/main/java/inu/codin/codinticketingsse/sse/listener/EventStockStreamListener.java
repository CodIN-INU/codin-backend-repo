package inu.codin.codinticketingsse.sse.listener;

import inu.codin.codinticketingsse.sse.dto.EventStockStream;
import inu.codin.codinticketingsse.sse.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventStockStreamListener implements StreamListener<String, MapRecord<String, String, String>> {

    private final SseService sseService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String STREAM_KEY = "event-stock-stream";
    private static final String GROUP_NAME = "event-stock-group";

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        try {
            Map<String, String> body = message.getValue();
            EventStockStream record = new EventStockStream(
                    Long.valueOf(body.get("eventId")),
                    Long.valueOf(body.get("quantity"))
            );

            log.info("[onMessage] 재고 상황 Stream 메세지 수신 eventId={}, quantity={}", record.eventId(), record.quantity());

            // SSE 전송
            sseService.publishEventStock(record);
            // Stream ACK 처리
            redisTemplate.opsForStream().acknowledge(STREAM_KEY, GROUP_NAME, message.getId());

        } catch (Exception ex) {
            log.error("StreamListener 처리 실패: id={}, error={}", message.getId(), ex.getMessage(), ex);
        }
    }
}
