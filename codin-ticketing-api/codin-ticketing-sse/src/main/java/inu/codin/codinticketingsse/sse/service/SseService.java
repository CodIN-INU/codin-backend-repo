package inu.codin.codinticketingsse.sse.service;

import inu.codin.codinticketingsse.sse.dto.SseStockResponse;
import inu.codin.codinticketingsse.sse.repository.SseEmitterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@Service
public class SseService {

    private final Logger log = LoggerFactory.getLogger(SseService.class);
    private final Long DEFAULT_TIMEOUT = 60 * 1000L;

    private final SseEmitterRepository sseEmitterRepository;

    public SseService(SseEmitterRepository sseEmitterRepository) {
        this.sseEmitterRepository = sseEmitterRepository;
    }

    /** 구독 시작 */
    public SseEmitter subscribeEventStock(Long eventId) {
        SseEmitter emitter = sseEmitterRepository.saveEmitter(eventId,  new SseEmitter(DEFAULT_TIMEOUT));
        log.info("Subscribed to event : {}, emitter : {}", eventId, emitter.toString());

        emitter.onCompletion(() -> sseEmitterRepository.deleteByEventIdAndEmitter(eventId, emitter));
        emitter.onTimeout(() -> sseEmitterRepository.deleteByEventIdAndEmitter(eventId, emitter));
        emitter.onError(e -> {
                    log.error("EventSource Failed : {}", e.getMessage());
                    sseEmitterRepository.deleteByEventIdAndEmitter(eventId, emitter);
        });

        sendAsyncToClient(emitter, "Init Subscribed to event : " + eventId, "ticketing-stock-sse");
        return emitter;
    }

    /** 이벤트 ID에 대해 payload 전송 */
    public void publishEventStock(Long eventId, Long quantity) {
        SseStockResponse data = SseStockResponse.ofNew(eventId, quantity);
        List<SseEmitter> list = sseEmitterRepository.findAll(eventId);

        log.info("[publishEventStock] Sent Event, size : {}, eventId : {}", list.size(), eventId);
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
    @Async
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
}
