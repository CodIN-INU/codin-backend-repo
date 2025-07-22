package inu.codin.codinticketingapi.domain.ticketing.service;

import inu.codin.codinticketingapi.domain.ticketing.dto.response.EventParticipationHistoryDto;
import inu.codin.codinticketingapi.domain.ticketing.entity.Participation;
import inu.codin.codinticketingapi.domain.ticketing.entity.ParticipationStatus;
import inu.codin.codinticketingapi.domain.ticketing.repository.ParticipationRepository;
import inu.codin.codinticketingapi.security.util.SecurityUtil;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.EventDetailResponse;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.EventPageResponse;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.EventParticipationHistoryPageResponse;
import inu.codin.codinticketingapi.domain.ticketing.entity.Campus;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingErrorCode;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingException;
import inu.codin.codinticketingapi.domain.ticketing.repository.EventRepository;
import inu.codin.codinticketingapi.domain.user.service.UserClientService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ParticipationRepository participationRepository;

    private final UserClientService userClientService;

    @Transactional(readOnly = true)
    public EventPageResponse getEventList(@Valid Campus campus, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("createdAt").descending());
        return EventPageResponse.of(eventRepository.findByCampus(campus, pageable));
    }

    @Transactional(readOnly = true)
    public EventDetailResponse getEventDetail(Long eventId) {
        return EventDetailResponse.of(eventRepository.findById(eventId)
                .orElseThrow(() -> new TicketingException(TicketingErrorCode.EVENT_NOT_FOUND)));
    }

    @Transactional(readOnly = true)
    public EventParticipationHistoryPageResponse getUserEventList(@NotNull int pageNumber) {
        String userId = userClientService.fetchUser().getUserId();
        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("createdAt").descending());
        return EventParticipationHistoryPageResponse.of(participationRepository.findHistoryByUserId(userId, pageable));
    }

    @Transactional(readOnly = true)
    public EventParticipationHistoryPageResponse getUserEventListByStatus(@NotNull int pageNumber, ParticipationStatus status) {
        String userId = userClientService.fetchUser().getUserId();
        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("createdAt").descending());
        return EventParticipationHistoryPageResponse.of(participationRepository.findHistoryByUserIdAndCanceled(userId, status, pageable));
    }
}