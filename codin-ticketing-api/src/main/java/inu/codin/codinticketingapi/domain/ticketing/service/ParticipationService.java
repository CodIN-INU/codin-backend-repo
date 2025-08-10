package inu.codin.codinticketingapi.domain.ticketing.service;

import inu.codin.codinticketingapi.domain.admin.entity.Event;
import inu.codin.codinticketingapi.domain.admin.entity.EventStatus;
import inu.codin.codinticketingapi.domain.ticketing.dto.event.ParticipationCreatedEvent;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.ParticipationResponse;
import inu.codin.codinticketingapi.domain.ticketing.entity.Participation;
import inu.codin.codinticketingapi.domain.ticketing.entity.ParticipationStatus;
import inu.codin.codinticketingapi.domain.ticketing.entity.Stock;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingErrorCode;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingException;
import inu.codin.codinticketingapi.domain.ticketing.redis.RedisParticipationService;
import inu.codin.codinticketingapi.domain.ticketing.repository.EventRepository;
import inu.codin.codinticketingapi.domain.ticketing.repository.ParticipationRepository;
import inu.codin.codinticketingapi.domain.user.dto.UserInfoResponse;
import inu.codin.codinticketingapi.domain.user.exception.UserErrorCode;
import inu.codin.codinticketingapi.domain.user.exception.UserException;
import inu.codin.codinticketingapi.domain.user.service.UserClientService;
import inu.codin.codinticketingapi.security.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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

    private final ApplicationEventPublisher eventPublisher;
    private final RedisParticipationService redisParticipationService;

    @Transactional
    public ParticipationResponse saveParticipation(Long eventId) {
        UserInfoResponse userInfoResponse = userClientService.fetchUser();
        // 유저 티켓팅 정보가 존재하는지 검증
        if (userInfoResponse.getDepartment() == null || userInfoResponse.getStudentId() == null) {
            throw new UserException(UserErrorCode.NOT_EXIST_PARTICIPATION_DATA);
        }
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new TicketingException(TicketingErrorCode.EVENT_NOT_FOUND));

        // 캐시에서 먼저 조회
        Optional<ParticipationResponse> cached = redisParticipationService.getCachedParticipation(userInfoResponse.getUserId(), eventId);
        if (cached.isPresent()) {
            return cached.get();
        }

        // 이미 참여한 사용자인지 확인
        Optional<Participation> existingParticipation = participationRepository.findByUserIdAndEvent(userInfoResponse.getUserId(), event);

        if (existingParticipation.isPresent()) {
            Participation participation = existingParticipation.get();
            // 취소 상태가 아니면 기존 참여 정보 반환
            if (participation.getStatus() != ParticipationStatus.CANCELED) {
                return ParticipationResponse.of(participation);
            }
            // CANCELED 상태면 재참여 가능 (아래 이벤트 참여 로직 진행)
        }

        // 이벤트 상태 검증
        if (event.getEventStatus() != EventStatus.ACTIVE) {
            throw new TicketingException(TicketingErrorCode.EVENT_NOT_ACTIVE);
        }
        // 재고 줄임
        Stock stock = ticketingService.decrement(eventId);

        // 사용자 번호표
        int ticketNumber = stock.getInitialStock() - stock.getStock();

        Participation participation = Participation.builder()
                .event(event)
                .ticketNumber(ticketNumber)
                .userInfoResponse(userInfoResponse)
                .build();

        Participation savedParticipation = participationRepository.save(participation);

        // 참여 생성 이벤트 발행
        eventPublisher.publishEvent(new ParticipationCreatedEvent(savedParticipation));

        return ParticipationResponse.of(savedParticipation);
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
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new TicketingException(TicketingErrorCode.EVENT_NOT_FOUND));
        Participation participation = participationRepository.findByUserIdAndEvent(userId, event)
                .orElseThrow(() -> new TicketingException(TicketingErrorCode.PARTICIPATION_NOT_FOUND));
        ParticipationResponse response = ParticipationResponse.of(participation);

        // 캐시에 저장
        redisParticipationService.cacheParticipation(userId, eventId, participation);

        return response;
    }
}