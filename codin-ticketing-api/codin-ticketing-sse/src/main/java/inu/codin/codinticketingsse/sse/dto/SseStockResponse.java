package inu.codin.codinticketingsse.sse.dto;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 유저에게 전송할 이벤트 재고상태 SSE 메세지 Dto
 * @param eventId 유저가 구독중인 이벤트 ID
 * @param quantity 이벤트 재고 수량
 * @param timestamp SSE 메세지 생성 시간
 */
@Builder
public record SseStockResponse(Long eventId, Long quantity, LocalDateTime timestamp) {

    public static SseStockResponse ofNew(EventStockStream stream) {
        return SseStockResponse.builder()
                .eventId(stream.eventId())
                .quantity(stream.quantity())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
