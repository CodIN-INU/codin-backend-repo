package inu.codin.codinticketingapi.domain.ticketing.service;

import inu.codin.codinticketingapi.domain.ticketing.entity.TicketingEvent;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingErrorCode;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingException;
import inu.codin.codinticketingapi.domain.ticketing.repository.EventRepository;
import inu.codin.codinticketingapi.common.util.ObjectIdUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventWriteService {

    private final EventRepository eventRepository;

    public void createEvent(/* 파라미터 */) {
        // 이벤트 생성 로직
        // 1. 이벤트 생성 유저 검증
        // 2. Request dto를 통한 이벤트 생성
        // 3. 저장된 이벤트를 기반으로 Redis에 이벤트 진행 세팅
    }

    public void updateEvent(/* 파라미터 */) {
        // 이벤트 수정 로직
        // 1. 이벤트 수정 유저 검증
        // 2. 수정된 이벤트 내용 검증
        // 3. 수정된 내용 중에서 Redis에 캐싱된 내용 Validation
    }

    public void deleteEvent(String eventId) {
        TicketingEvent event = eventRepository.findByIdAndNotDeleted(ObjectIdUtil.toObjectId(eventId))
                .orElseThrow(() -> new TicketingException(TicketingErrorCode.EVENT_NOT_FOUND));
        event.delete();
        eventRepository.save(event);
    }
}