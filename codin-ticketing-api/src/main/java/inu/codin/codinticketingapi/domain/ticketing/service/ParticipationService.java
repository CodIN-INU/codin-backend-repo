package inu.codin.codinticketingapi.domain.ticketing.service;

import inu.codin.codinticketingapi.domain.admin.entity.Event;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.EventParticipationHistoryDto;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.ParticipationCreateResponse;
import inu.codin.codinticketingapi.domain.ticketing.entity.Participation;
import inu.codin.codinticketingapi.domain.ticketing.entity.ParticipationStatus;
import inu.codin.codinticketingapi.domain.ticketing.entity.Stock;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingErrorCode;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingException;
import inu.codin.codinticketingapi.domain.ticketing.repository.EventRepository;
import inu.codin.codinticketingapi.domain.ticketing.repository.ParticipationRepository;
import inu.codin.codinticketingapi.domain.user.dto.UserInfoResponse;
import inu.codin.codinticketingapi.domain.user.service.UserClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParticipationService {

    private final TicketingService ticketingService;
    private final UserClientService userClientService;
    private final EventRepository eventRepository;
    private final ParticipationRepository participationRepository;

    @Deprecated
    @Transactional(readOnly = true)
    public Page<EventParticipationHistoryDto> getUserEventHistory(String userId, Pageable pageable) {
        return participationRepository.findHistoryByUserId(userId, pageable);
    }

    @Deprecated
    @Transactional(readOnly = true)
    public Page<EventParticipationHistoryDto> getUserEventHistoryByCanceled(String userId, ParticipationStatus status, Pageable pageable) {
        return participationRepository.findHistoryByUserIdAndCanceled(userId, status, pageable);
    }

    @Transactional
    public ParticipationCreateResponse saveParticipation(Long eventId) {
        UserInfoResponse userInfoResponse = userClientService.fetchUser();
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new TicketingException(TicketingErrorCode.EVENT_NOT_FOUND));
        Stock stock = ticketingService.decrement(eventId);

        // 사용자 번호표
        int ticketNumber = stock.getInitialStock() - stock.getStock();

        Participation participation = Participation.builder()
                .event(event)
                .ticketNumber(ticketNumber)
                .userInfoResponse(userInfoResponse)
                .build();

        return ParticipationCreateResponse.of(participationRepository.save(participation));
    }
}