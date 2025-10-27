package inu.codin.codinticketingsse.sse.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class SseEmitterTimeoutEvent {

    private final String emitterId;
    private final Long eventId;
    private final String userId;
    private final LocalDateTime timestamp;

    public static SseEmitterTimeoutEvent of(String emitterId, Long eventId, String userId) {
        return new SseEmitterTimeoutEvent(emitterId, eventId, userId, LocalDateTime.now());
    }
}
