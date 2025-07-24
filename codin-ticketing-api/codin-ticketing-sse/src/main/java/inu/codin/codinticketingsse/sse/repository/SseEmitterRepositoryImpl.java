package inu.codin.codinticketingsse.sse.repository;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class SseEmitterRepositoryImpl implements SseEmitterRepository {

    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>> emitterMap = new ConcurrentHashMap<>();;

    @Override
    public SseEmitter saveEmitter(Long eventId, SseEmitter emitter) {
        emitterMap.computeIfAbsent(eventId, id -> new CopyOnWriteArrayList<>())
                .add(emitter);
        return emitter;
    }

    @Override
    public List<SseEmitter> findAll(Long eventId) {
        return emitterMap.getOrDefault(eventId, new CopyOnWriteArrayList<>());
    }

    @Override
    public void deleteByEventIdAndEmitter(Long eventId, SseEmitter emitter) {
        emitterMap.computeIfPresent(eventId, (id, list) -> {
            list.remove(emitter);
            return list.isEmpty() ? null : list;
        });
    }

    @Override
    public void addEvent(Long evenId) {
        emitterMap.putIfAbsent(evenId, new CopyOnWriteArrayList<>());
    }

    @Override
    public void removeEvent(Long evenId) {
        emitterMap.remove(evenId);
    }
}