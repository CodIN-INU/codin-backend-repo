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
import inu.codin.codinticketingapi.domain.user.exception.UserErrorCode;
import inu.codin.codinticketingapi.domain.user.exception.UserException;
import inu.codin.codinticketingapi.domain.user.service.UserClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
        // 유저 티켓팅 정보가 존재하는지 검증
        if (userInfoResponse.getDepartment() == null || userInfoResponse.getStudentId() == null) {
            throw new UserException(UserErrorCode.NOT_EXIST_PARTICIPATION_DATA);
        }
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new TicketingException(TicketingErrorCode.EVENT_NOT_FOUND));

        // 이미 참여한 사용자인지 확인
        Optional<Participation> existingParticipation = participationRepository.findByUserIdAndEvent(userInfoResponse.getUserId(), event);

        if (existingParticipation.isPresent()) {
            // 이미 참여한 경우 기존 참여 내용 반환
            return ParticipationCreateResponse.of(existingParticipation.get());
        }

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