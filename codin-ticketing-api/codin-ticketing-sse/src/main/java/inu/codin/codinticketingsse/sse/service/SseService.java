package inu.codin.codinticketingsse.sse.service;

import inu.codin.codinticketingsse.sse.dto.EventStockStream;
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
    private final Long DEFAULT_TIMEOUT = 5 * 60 * 1000L;

    // todo : 이벤트 상황이 아닐 때에는 HearBeat 기능 추가

    private final SseEmitterRepository sseEmitterRepository;

    public SseService(SseEmitterRepository sseEmitterRepository) {
        this.sseEmitterRepository = sseEmitterRepository;
    }

    /** SSE 구독 시작 */
    public SseEmitter subscribeEventStock(Long eventId) {
        SseEmitter emitter = sseEmitterRepository.saveEmitter(eventId,  new SseEmitter(DEFAULT_TIMEOUT));
        log.info("SSE 구독 시작, event : {}, emitter : {}", eventId, emitter.toString());

        emitter.onCompletion(() -> sseEmitterRepository.deleteByEventIdAndEmitter(eventId, emitter));
        emitter.onTimeout(() -> sseEmitterRepository.deleteByEventIdAndEmitter(eventId, emitter));
        emitter.onError(e -> {
                    log.warn("Emitter onError 처리 : {}", e.getMessage());
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
}
