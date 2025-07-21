package inu.codin.codinticketingsse.sse.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record SseStockResponse(Long eventId, Long quantity, LocalDateTime timestamp) {

    public static SseStockResponse ofNew(Long eventId, Long quantity) {
        return SseStockResponse.builder()
                .eventId(eventId)
                .quantity(quantity)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
