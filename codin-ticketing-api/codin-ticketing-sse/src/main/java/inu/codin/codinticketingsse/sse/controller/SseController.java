package inu.codin.codinticketingsse.sse.controller;

import inu.codin.codinticketingsse.sse.dto.EventStockStream;
import inu.codin.codinticketingsse.sse.service.SseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@Tag(name = "재고상태 SSE API", description = "이벤트 재고상태 SSE 구독, 전송")
public class SseController {

    private final SseService sseService;

    public SseController(SseService sseService) {
        this.sseService = sseService;
    }

    @GetMapping(value = "sse/{eventId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "이벤트 재고상태 SSE 구독")
    public ResponseEntity<SseEmitter> subscribeEvent(
            @Parameter(description = "구독한 이벤트 ID", example = "1111") @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(sseService.subscribeEventStock(eventId));
    }

    @PostMapping(value = "sse/{eventId}")
    @Operation(summary = "[테스트] 재고상태 SSE 전송")
    public ResponseEntity<?> sendQuantityUpdateEvent(
            @Parameter(description = "구독한 이벤트 ID", example = "1111") @PathVariable Long eventId,
            @Parameter(description = "전송할 임의 재고 상태", example = "100") @RequestParam Long quantity
    ) {
        sseService.publishEventStock(new EventStockStream(eventId, quantity));
        return ResponseEntity.ok().build();
    }
}
