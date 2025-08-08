package inu.codin.codinticketingsse.sse.monitor;

import inu.codin.codinticketingsse.sse.dto.SseEmitterTimeoutEvent;
import inu.codin.codinticketingsse.sse.repository.SseEmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseConnectionMonitor {

    private final SseEmitterRepository sseEmitterRepository;

    @EventListener
    @Async
    public void handleEmitterTimeout(SseEmitterTimeoutEvent event) {
        log.info("SSE 연결 Timeout: emitterId={}, eventId={}, userId={}, timestamp={}",
                event.getEmitterId(), event.getEventId(), event.getUserId(), event.getTimestamp());

        // 타임아웃된 연결에 대한 추가 정리 작업
        try {
            sseEmitterRepository.removeEmitter(event.getEventId(), event.getUserId());
            log.info("타임아웃된 SSE 연결 정리 완료: eventId={}, userId={}",
                    event.getEventId(), event.getUserId());
        } catch (Exception e) {
            log.error("타임아웃된 SSE 연결 정리 실패: eventId={}, userId={}, error={}",
                    event.getEventId(), event.getUserId(), e.getMessage());
        }
    }

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void logConnectionStats() {
        int activeConnections = sseEmitterRepository.getActiveConnectionCount();
        log.info("활성 SSE 연결 수: {}", activeConnections);

        if (activeConnections > 1000) {
            log.warn("SSE 연결 수가 임계값을 초과했습니다: {}", activeConnections);
        }
    }

    @Scheduled(fixedRate = 300000) // 5분마다 실행
    public void cleanupDeadConnections() {
        log.info("SSE 연결 상태 점검 시작");

        sseEmitterRepository.getAllEmitters().forEach((key, emitter) -> {
            try {
                // ping 테스트
                emitter.send("ping");
            } catch (Exception e) {
                // 응답하지 없는 연결 정리
                String[] keyParts = key.split(":");
                if (keyParts.length == 2) {
                    Long eventId = Long.valueOf(keyParts[0]);
                    String userId = keyParts[1];
                    sseEmitterRepository.removeEmitter(eventId, userId);
                    log.info("비활성 SSE 연결 정리: key={}", key);
                }
            }
        });

        log.info("SSE 연결 상태 점검 완료");
    }
}
