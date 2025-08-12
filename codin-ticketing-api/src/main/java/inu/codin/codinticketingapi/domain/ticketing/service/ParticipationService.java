package inu.codin.codinticketingapi.domain.ticketing.service;

import inu.codin.codinticketingapi.domain.admin.entity.Event;
import inu.codin.codinticketingapi.domain.admin.entity.EventStatus;
import inu.codin.codinticketingapi.domain.ticketing.dto.event.ParticipationCreatedEvent;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.ParticipationResponse;
import inu.codin.codinticketingapi.domain.ticketing.entity.Department;
import inu.codin.codinticketingapi.domain.ticketing.entity.Participation;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingErrorCode;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingException;
import inu.codin.codinticketingapi.domain.ticketing.redis.RedisEventService;
import inu.codin.codinticketingapi.domain.ticketing.redis.RedisParticipationService;
import inu.codin.codinticketingapi.domain.ticketing.repository.EventRepository;
import inu.codin.codinticketingapi.domain.ticketing.repository.ParticipationRepository;
import inu.codin.codinticketingapi.domain.user.dto.UserInfoResponse;
import inu.codin.codinticketingapi.domain.user.exception.UserErrorCode;
import inu.codin.codinticketingapi.domain.user.exception.UserException;
import inu.codin.codinticketingapi.domain.user.service.UserClientService;
import inu.codin.codinticketingapi.security.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParticipationService {

    private final TicketingService ticketingService;
    private final UserClientService userClientService;
    private final RedisEventService redisEventService;

    private final EventRepository eventRepository;
    private final ParticipationRepository participationRepository;

    private final ApplicationEventPublisher eventPublisher;
    private final RedisParticipationService redisParticipationService;

    @Transactional
    public ParticipationResponse saveParticipation(Long eventId) {
        UserInfoResponse userInfoResponse = userClientService.fetchUser();
        Event findEvent = findEvent(eventId);

        // 유저 정보가 존재하는지 검증
        if (userInfoResponse.getDepartment() == null || userInfoResponse.getStudentId() == null) {
            throw new UserException(UserErrorCode.NOT_EXIST_PARTICIPATION_DATA);
        }

        // 이벤트 상태 검증
        if (findEvent.getEventStatus() != EventStatus.ACTIVE) {
            throw new TicketingException(TicketingErrorCode.EVENT_NOT_ACTIVE);
        }

       return findParticipationResponse(userInfoResponse.getUserId(), findEvent.getId())
               .orElseGet(() -> createParticipation(findEvent, userInfoResponse));
    }

    @Transactional(readOnly = true)
    public ParticipationResponse findParticipationByEvent(Long eventId) {
        String userId = SecurityUtil.getUserId();

        // 캐시에서 먼저 조회
        Optional<ParticipationResponse> cached = redisParticipationService.getCachedParticipation(userId, eventId);
        if (cached.isPresent()) {
            return cached.get();
        }

        // 캐시 미스 시 DB 조회
        Event event = findEvent(eventId);
        Participation participation = participationRepository.findByUserIdAndEvent(userId, event)
                .orElseThrow(() -> new TicketingException(TicketingErrorCode.PARTICIPATION_NOT_FOUND));
        ParticipationResponse response = ParticipationResponse.of(participation);

        // 캐시에 저장
        redisParticipationService.cacheParticipation(userId, eventId, participation);

        return response;
    }

    private Event findEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new TicketingException(TicketingErrorCode.EVENT_NOT_FOUND));
    }

    private ParticipationResponse createParticipation(Event event, UserInfoResponse userInfoResponse) {
        try {
            // 재고 줄임
            ticketingService.decrement(event.getId());
            Integer ticketNumber = redisEventService.getTicket(event.getId());
            log.info("ticketNumber: {}", ticketNumber);

            Participation participation = Participation.builder()
                    .event(event)
                    .ticketNumber(ticketNumber)
                    .userInfoResponse(userInfoResponse)
                    .build();

            Participation savedParticipation = participationRepository.save(participation);

            // 참여 생성 이벤트 발행
            eventPublisher.publishEvent(new ParticipationCreatedEvent(savedParticipation));
            log.info("새로운 참가자 : {}", event.getId());
            return ParticipationResponse.of(savedParticipation);
        } catch (DataIntegrityViolationException dup) {

            return participationRepository.findByUserIdAndEventIdAndNotCanceled(userInfoResponse.getUserId(), event.getId())
                    .map(ParticipationResponse::of)
                    .orElseThrow(() -> dup);
        }
    }

    private Optional<ParticipationResponse> findParticipationResponse(String userId, Long eventId) {
        // 1. 캐시에서 먼저 조회
        Optional<ParticipationResponse> cached = redisParticipationService.getCachedParticipation(userId, eventId);
        log.info("cache hit : {}", eventId);

        if (cached.isPresent()) {

            return cached;
        }

        // 2. 캐시에 없으면 DB에서 조회 및 상태 확인
        return participationRepository.findByUserIdAndEventIdAndNotCanceled(userId, eventId)
                .map(ParticipationResponse::of);
    }
}