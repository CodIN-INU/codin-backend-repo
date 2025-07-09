package inu.codin.codinticketingapi.domain.ticketing.service;

import inu.codin.codinticketingapi.domain.ticketing.dto.response.EventDetailResponse;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.EventPageResponse;
import inu.codin.codinticketingapi.domain.ticketing.entity.Campus;
import inu.codin.codinticketingapi.domain.ticketing.entity.TicketingEvent;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingErrorCode;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingException;
import inu.codin.codinticketingapi.domain.ticketing.repository.EventRepository;
import inu.codin.codinticketingapi.common.util.ObjectIdUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventReadService {

    private final EventRepository eventRepository;

    public EventPageResponse getEventList(Campus campus, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("createdAt").descending());
        Page<TicketingEvent> page = eventRepository.getTicketingEventsByCampus(campus.toString(), pageable);
        return EventPageResponse.of(page);
    }

    public EventDetailResponse getEventDetail(String eventId) {
        TicketingEvent event = eventRepository.findByIdAndNotDeleted(ObjectIdUtil.toObjectId(eventId))
                .orElseThrow(() -> new TicketingException(TicketingErrorCode.EVENT_NOT_FOUND));
        return EventDetailResponse.of(event);
    }

    public Object getEventListByManager(@Valid Campus campus, @NotNull int pageNumber) {

    }
}