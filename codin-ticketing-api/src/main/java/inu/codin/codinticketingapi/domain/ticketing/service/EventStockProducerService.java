package inu.codin.codinticketingapi.domain.ticketing.service;

import inu.codin.codinticketingapi.domain.ticketing.dto.stream.EventStockStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventStockProducerService {

    @Value("${redis.stream.stock.key}")
    private String STREAM_KEY;

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * EventStockStream DTO를 Redis Stream에 전송
     */
    public void publishEventStock(EventStockStream eventStockStream) {
        // stock_event_log 테이블 추가해 로그 관리
        ObjectRecord<String, EventStockStream> record = StreamRecords
                .newRecord()
                .in(STREAM_KEY)
                .ofObject(eventStockStream);

        RecordId recordId = redisTemplate.opsForStream().add(record);

        log.info("[Producer] Published EventStockStream: {}", eventStockStream);
//        return recordId;
    }
}
