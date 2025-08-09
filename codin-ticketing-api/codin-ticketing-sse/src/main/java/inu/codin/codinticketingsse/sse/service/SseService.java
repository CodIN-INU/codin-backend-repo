package inu.codin.codinticketingsse.sse.service;

import inu.codin.codinticketingsse.sse.dto.EventStockStream;
import inu.codin.codinticketingsse.sse.dto.SseEmitterTimeoutEvent;
import inu.codin.codinticketingsse.sse.dto.SseStockResponse;
import inu.codin.codinticketingsse.sse.repository.SseEmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SseService {

    private final Long DEFAULT_TIMEOUT = 10 * 60 * 1000L; // 10분

    private final SseEmitterRepository sseEmitterRepository;
    private final ApplicationEventPublisher eventPublisher;

    /** SSE 구독 시작 */
    public SseEmitter subscribeEventStock(Long eventId, String userId) {
        SseEmitter emitter = sseEmitterRepository.saveEmitter(eventId, userId, new SseEmitter(DEFAULT_TIMEOUT));
        log.info("SSE 구독 시작, event : {}, userId : {}, emitter : {}", eventId, userId, emitter.toString());

        emitter.onCompletion(() -> {
            sseEmitterRepository.deleteByEventIdAndEmitter(eventId, emitter);
            log.info("SSE 연결 완료 - eventId: {}, userId: {}", eventId, userId);
        });

        emitter.onTimeout(() -> {
            sseEmitterRepository.deleteByEventIdAndEmitter(eventId, emitter);
            eventPublisher.publishEvent(SseEmitterTimeoutEvent.of(
                    generateKey(eventId, userId), eventId, userId));
            log.info("SSE 연결 타임아웃 - eventId: {}, userId: {}", eventId, userId);
        });

        emitter.onError(e -> {
            log.warn("Emitter onError 처리 - eventId: {}, userId: {}, error: {}", eventId, userId, e.getMessage());
            sseEmitterRepository.deleteByEventIdAndEmitter(eventId, emitter);
        });

        sendAsyncToClient(emitter, "Init Subscribed to event : " + eventId, "ticketing-stock-sse");
        return emitter;
    }

    /** 이벤트 ID에 대해 SSE payload 전송 */
    public void publishEventStock(EventStockStream stream) {
        SseStockResponse data = SseStockResponse.ofNew(stream);
        List<SseEmitter> list = sseEmitterRepository.findAll(data.eventId());

        log.info("[publishEventStock] 재고상태 SSE 전송, size : {}, eventId : {}", list.size(), data.eventId());
        for (SseEmitter emitter : list) {
            sendAsyncToClient(emitter, data, "ticketing-stock-sse");
        }
    }

    /**
     * SSE 전송
     * @param emitter SSE 에미터
     * @param data Object 데이터
     * @param name 이벤트 이름
     */
    @Async("taskExecutor")
    public void sendAsyncToClient(SseEmitter emitter, Object data, String name) {
        try {
            emitter.send(SseEmitter.event()
                    .name(name)
                    .data(data));
        } catch (IOException | IllegalStateException ex) {
            // onError 콜백 실행
            emitter.completeWithError(ex);
        }
    }

    /**
     * HeartBeat를 연결 중인 모든 Emitter에 전송하여 비활성 연결 정리
     * Scheduled 30초
     */
    @Scheduled(fixedRate = 30000)
    public void deleteDeadConnections() {
        sendHeartbeatAllEmitters();
    }

    public void sendHeartbeatAllEmitters() {
        sseEmitterRepository.getAllEmitters().forEach((key, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("heartbeat")
                        .data("ping"));
            } catch (Exception e) {
                // 연결이 끊어진 Emitter 제거
                String[] keyParts = key.split(":");
                if (keyParts.length == 2) {
                    Long eventId = Long.valueOf(keyParts[0]);
                    String userId = keyParts[1];
                    sseEmitterRepository.removeEmitter(eventId, userId);
                    log.info("HeartBeat 실패로 SSE 연결 제거: key={}", key);
                }
            }
        });
    }

    /**
     * SSE Emitter 연결 종료
     * @param eventId connection 유지 중인 event
     * @param userId String userId (MongoDB)
     */
    public void closeConnection(Long eventId, String userId) {
        SseEmitter emitter = sseEmitterRepository.findEmitter(eventId, userId);
        if (emitter != null) {
            emitter.complete(); // 정상적으로 연결 종료
            sseEmitterRepository.removeEmitter(eventId, userId);
            log.info("SSE 연결 수동 종료 - eventId: {}, userId: {}", eventId, userId);
        }
    }

    /**
     * 키 생성 헬퍼 메서드
     */
    private String generateKey(Long eventId, String userId) {
        return eventId + ":" + userId;
    }
}
