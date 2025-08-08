package inu.codin.codinticketingsse.sse.repository;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public interface SseEmitterRepository {
    SseEmitter saveEmitter(Long eventId, String userId, SseEmitter emitter);
    SseEmitter findEmitter(Long eventId, String userId);
    List<SseEmitter> findAll(Long eventId);
    void deleteByEventIdAndEmitter(Long eventId, SseEmitter emitter);
    void removeEmitter(Long eventId, String userId);
    ConcurrentHashMap<String, SseEmitter> getAllEmitters();
    int getActiveConnectionCount();
}
