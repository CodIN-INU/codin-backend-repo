package inu.codin.codinticketingapi.domain.ticketing.service;

import inu.codin.codinticketingapi.domain.admin.entity.Event;
import inu.codin.codinticketingapi.domain.admin.entity.EventStatus;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.EventDetailResponse;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.EventPageResponse;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.EventParticipationHistoryPageResponse;
import inu.codin.codinticketingapi.domain.ticketing.entity.Campus;
import inu.codin.codinticketingapi.domain.ticketing.entity.ParticipationStatus;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingErrorCode;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingException;
import inu.codin.codinticketingapi.domain.ticketing.repository.EventRepository;
import inu.codin.codinticketingapi.domain.ticketing.repository.ParticipationRepository;
import inu.codin.codinticketingapi.domain.user.dto.UserInfoResponse;
import inu.codin.codinticketingapi.domain.user.service.UserClientService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ParticipationRepository participationRepository;

    private final UserClientService userClientService;
    private final ParticipationService participationService;

    @Transactional(readOnly = true)
    public EventPageResponse getEventList(@NotNull Campus campus, @PositiveOrZero int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, 10);

        return EventPageResponse.from(eventRepository.findByCampus(campus, pageable));
    }

    @Transactional(readOnly = true)
    public EventDetailResponse getEventDetail(Long eventId) {
        UserInfoResponse userInfoResponse = userClientService.fetchUser();
        // 유저 티켓팅 정보가 존재하는지 검증
        boolean isExistParticipationData = userInfoResponse.getDepartment() != null && userInfoResponse.getStudentId() != null;
        boolean isUserParticipatedInEvent = participationService.isUserParticipatedInEvent(eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new TicketingException(TicketingErrorCode.EVENT_NOT_FOUND));

        return EventDetailResponse.of(event, isExistParticipationData, isUserParticipatedInEvent);
    }

    @Transactional(readOnly = true)
    public EventParticipationHistoryPageResponse getUserEventList(int pageNumber) {
        String userId = userClientService.fetchUser().getUserId();
        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("createdAt").descending());
        return EventParticipationHistoryPageResponse.of(participationRepository.findHistoryByUserId(userId, pageable));
    }

    @Transactional(readOnly = true)
    public EventParticipationHistoryPageResponse getUserEventListByStatus(int pageNumber, @NotNull ParticipationStatus status) {
        String userId = userClientService.fetchUser().getUserId();
        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("createdAt").descending());
        return EventParticipationHistoryPageResponse.of(participationRepository.findHistoryByUserIdAndCanceled(userId, status, pageable));
    }

    @Transactional
    public void changeAllActiveEventsToUpcoming() {
        List<Event> activeEvents = eventRepository.findByEventStatus(EventStatus.ACTIVE);

        if (activeEvents.isEmpty()) {
            log.info("상태를 변경할 활성 이벤트가 없습니다.");

            return;
        }

        // 각 이벤트의 상태를 UPCOMING으로 변경
        activeEvents.forEach(event -> {
            log.info("이벤트 ID: {}, '{}'의 상태를 ACTIVE에서 UPCOMING으로 변경합니다.", event.getId(), event.getTitle());
            event.updateStatus(EventStatus.UPCOMING);
        });
    }

    @Transactional
    public void restoreUpcomingEventsToActive() {
        List<Event> upcomingEvents = eventRepository.findAllLiveEvent(EventStatus.UPCOMING, LocalDateTime.now());

        if (upcomingEvents.isEmpty()) {
            log.info("ACTIVE로 복구할 UPCOMING 상태의 이벤트가 없습니다.");

            return;
        }

        upcomingEvents.forEach(event -> {
            log.info("Redis 복구로 인해 이벤트 ID: {}의 상태를 ACTIVE로 복구합니다.", event.getId());
            event.updateStatus(EventStatus.ACTIVE);
        });
    }
}