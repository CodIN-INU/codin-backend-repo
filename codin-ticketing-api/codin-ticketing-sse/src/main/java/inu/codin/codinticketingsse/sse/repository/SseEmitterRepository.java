package inu.codin.codinticketingsse.sse.repository;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface SseEmitterRepository {
    SseEmitter saveEmitter(Long eventId, SseEmitter emitter);
    List<SseEmitter> findAll(Long eventId);
    void deleteByEventIdAndEmitter(Long eventId, SseEmitter emitter);

    void addEvent(Long evenId);
    void removeEvent(Long evenId);
}
