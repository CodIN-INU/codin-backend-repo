package inu.codin.codinticketingsse.sse.controller;

import inu.codin.codinticketingsse.sse.service.SseService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class SseController {

    private final SseService sseService;

    public SseController(SseService sseService) {
        this.sseService = sseService;
    }

    @GetMapping(value = "sse/{eventId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribeEvent(
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(sseService.subscribeEventStock(eventId));
    }

    @PostMapping(value = "sse/{eventId}")
    public ResponseEntity<?> sendQuantityUpdateEvent(
            @PathVariable Long eventId,
            @RequestParam Long quantity
    ) {
        sseService.publishEventStock(eventId, quantity);
        return ResponseEntity.ok().build();
    }
}
