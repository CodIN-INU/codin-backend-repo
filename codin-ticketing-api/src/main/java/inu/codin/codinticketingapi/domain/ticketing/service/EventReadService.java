package inu.codin.codinticketingapi.domain.ticketing.service;

import inu.codin.codinticketingapi.common.security.util.SecurityUtil;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.EventDetailResponse;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.EventPageResponse;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.EventParticipationHistoryDto;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.EventParticipationHistoryPageResponse;
import inu.codin.codinticketingapi.domain.ticketing.entity.Campus;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingErrorCode;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingException;
import inu.codin.codinticketingapi.domain.ticketing.repository.EventRepository;
import inu.codin.codinticketingapi.domain.user.service.UserClientService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventReadService {

    private final EventRepository eventRepository;
    private final UserClientService userClientService;
    private final ParticipationReadService participationReadService;

    public EventPageResponse getEventList(@Valid Campus campus, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("createdAt").descending());
        return EventPageResponse.of(eventRepository.findByCampus(campus, pageable));
    }

    public EventDetailResponse getEventDetail(String eventId) {
        return EventDetailResponse.of(eventRepository.findById(eventId)
                .orElseThrow(() -> new TicketingException(TicketingErrorCode.EVENT_NOT_FOUND)));
    }

    public EventPageResponse getEventListByManager(int pageNumber) {
        String userId = userClientService.fetchUserIdAndUsername(SecurityUtil.getEmail()).userId();
        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("createdAt").descending());
        return EventPageResponse.of(eventRepository.findByCreatedUserId(userId, pageable));
    }

    public EventParticipationHistoryPageResponse getUserEventList(@NotNull int pageNumber) {
        String userId = userClientService.fetchUserIdAndUsername(SecurityUtil.getEmail()).userId();
        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("createdAt").descending());
        return EventParticipationHistoryPageResponse.of(participationReadService.getUserEventHistory(userId, pageable));
    }

    public EventParticipationHistoryPageResponse getUserEventListByCanceled(@NotNull int pageNumber, boolean canceled) {
        String userId = userClientService.fetchUserIdAndUsername(SecurityUtil.getEmail()).userId();
        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("createdAt").descending());
        return EventParticipationHistoryPageResponse.of(participationReadService.getUserEventHistoryByCanceled(userId, pageable, canceled));
    }
}