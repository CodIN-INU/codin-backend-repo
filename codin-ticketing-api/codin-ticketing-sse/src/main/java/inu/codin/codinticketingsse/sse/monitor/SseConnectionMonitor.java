package inu.codin.codinticketingsse.sse.monitor;

import inu.codin.codinticketingsse.sse.dto.SseEmitterTimeoutEvent;
import inu.codin.codinticketingsse.sse.repository.SseEmitterRepository;
import inu.codin.codinticketingsse.sse.service.SseService;
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
    private final SseService sseService;

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
        sseService.sendHeartbeatAllEmitters();
        log.info("SSE 연결 상태 점검 완료");
    }
}
