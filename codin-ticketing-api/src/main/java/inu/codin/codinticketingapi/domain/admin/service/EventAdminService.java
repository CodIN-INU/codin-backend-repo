package inu.codin.codinticketingapi.domain.admin.service;

import inu.codin.codinticketingapi.domain.admin.dto.request.EventCreateRequest;
import inu.codin.codinticketingapi.domain.admin.dto.request.EventUpdateRequest;
import inu.codin.codinticketingapi.domain.admin.dto.response.EventParticipationProfilePageResponse;
import inu.codin.codinticketingapi.domain.admin.dto.response.EventParticipationProfileResponse;
import inu.codin.codinticketingapi.domain.admin.dto.response.EventResponse;
import inu.codin.codinticketingapi.domain.admin.dto.response.EventStockResponse;
import inu.codin.codinticketingapi.domain.admin.entity.Event;
import inu.codin.codinticketingapi.domain.admin.entity.EventStatus;
import inu.codin.codinticketingapi.domain.admin.scheduler.EventStatusScheduler;
import inu.codin.codinticketingapi.domain.image.service.ImageService;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.EventPageResponse;
import inu.codin.codinticketingapi.domain.ticketing.entity.Participation;
import inu.codin.codinticketingapi.domain.ticketing.entity.Stock;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingErrorCode;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingException;
import inu.codin.codinticketingapi.domain.ticketing.repository.EventRepository;
import inu.codin.codinticketingapi.domain.ticketing.repository.ParticipationRepository;
import inu.codin.codinticketingapi.domain.user.exception.UserErrorCode;
import inu.codin.codinticketingapi.domain.user.exception.UserException;
import inu.codin.codinticketingapi.domain.user.service.UserClientService;
import inu.codin.codinticketingapi.infra.redis.RedisEventService;
import inu.codin.codinticketingapi.security.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventAdminService {

    private final EventRepository eventRepository;
    private final ParticipationRepository participationRepository;

    private final static int PAGE_SIZE = 10;

    private final ImageService imageService;
    private final UserClientService userClientService;
    private final RedisEventService redisEventService;
    private final EventStatusScheduler eventStatusScheduler;

    @Transactional
    public EventResponse createEvent(EventCreateRequest request, MultipartFile eventImage) {
        String userId = findAdminUser();
        request.validateEventTimes();

        String eventImageUrl = imageService.handleImageUpload(eventImage);
        Event event = request.toEntity(userId, eventImageUrl);
        Stock stock = Stock.builder()
                .event(event)
                .initialStock(request.getQuantity())
                .build();

        Event savedEvent = eventRepository.save(event);
        redisEventService.initializeEvent(savedEvent.getId(), stock.getStock(), savedEvent.getEventEndTime());
        eventStatusScheduler.scheduleCreateOrUpdatedEvent(savedEvent);

        return EventResponse.of(savedEvent);
    }

    public EventPageResponse getEventListByManager(String status, int pageNumber) {
        findAdminUser();

        return eventPageResponseWithStatus(status, pageNumber - 1);
    }

    @Transactional
    public EventResponse updateEvent(Long eventId, EventUpdateRequest request, MultipartFile eventImage) {
        // 엔티티 조회, 권한 검증
        Event event = findEventById(eventId);
        String currentUserId = findAdminUser();

        // 입력값 검증
        request.validateEventTimes();
        validationEvent(event, currentUserId);

        // 수량 변경 대비
        int oldQuantity = event.getStock().getStock();

        // 이미지 처리
        if (eventImage != null && !eventImage.isEmpty()) {
            String newUrl = imageService.handleImageUpload(eventImage);
            event.updateImageUrl(newUrl);
        }

        // 엔티티 업데이트
        event.updateFrom(request);

        // Redis 동기화 - (수량 변경 시)
        int newQuantity = event.getStock().getStock();

        if (newQuantity != oldQuantity) {
            redisEventService.initializeEvent(event.getId(), event.getStock().getStock(), event.getEventEndTime());
        }

        eventStatusScheduler.scheduleCreateOrUpdatedEvent(event);

        return EventResponse.of(event);
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = findEventById(eventId);
        event.delete();
        redisEventService.deleteEvent(eventId);
        eventStatusScheduler.scheduleAllDelete(event);
    }

    public String getEventPassword(Long eventId) {

        return eventRepository.findById(eventId)
                .orElseThrow(() -> new TicketingException(TicketingErrorCode.EVENT_NOT_FOUND))
                .getEventPassword();
    }

    @Transactional
    public void closeEvent(Long eventId) {
        Event findEvent = findEventById(eventId);
        findEvent.delete();
        redisEventService.deleteEvent(eventId);
        eventStatusScheduler.scheduleAllDelete(findEvent);
    }

    public EventParticipationProfilePageResponse getParticipationList(Long eventId, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("ticketNumber").descending());

        Page<Participation> participationList = participationRepository.findAllByEvent_Id(eventId, pageable);
        List<EventParticipationProfileResponse> profileList = participationList.stream()
                .map(p -> EventParticipationProfileResponse.of(p.getProfile(), p.getSignatureImgUrl()))
                .toList();

        int lastPage = getLastPage(participationList.getTotalPages());
        int nextPage = getNextPage(participationList.hasNext(), participationList.getNumber());

        return new EventParticipationProfilePageResponse(profileList, lastPage, nextPage);
    }

    @Transactional
    public boolean changeReceiveStatus(Long eventId, String userId) {
        Participation findParticipation = getParticipationByEventIdAndUserId(eventId, userId);
        findParticipation.changeConfirmStatus();

        return true;
    }

    @Transactional(readOnly = true)
    public EventStockResponse getStock(Long eventId) {
        Event findEvent = findEventById(eventId);
        Stock stock = findEvent.getStock();

        return EventStockResponse.of(stock.getStock());
    }

    public Boolean cancelTicket(Long eventId, String userId) {
        Participation findParticipation = getParticipationByEventIdAndUserId(eventId, userId);
        participationRepository.deleteById(findParticipation.getId());

        return true;
    }

    @Transactional
    public Boolean openEvent(Long eventId) {
        Event findEvent = findEventById(eventId);

        if (findEvent.getEventStatus() == EventStatus.ACTIVE) {

            return true;
        }

        findEvent.updateStatus(EventStatus.ACTIVE);
        eventStatusScheduler.deleteOpenEventScheduler(eventId);

        return true;
    }

    private EventPageResponse eventPageResponseWithStatus(String status, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, PAGE_SIZE, Sort.by("createdAt").descending());

        return switch (status) {
            case "all" -> EventPageResponse.of(eventRepository.findAll(pageable));
            case "upcoming" -> EventPageResponse.of(eventRepository.findAllByEventStatus(EventStatus.UPCOMING, pageable));
            case "open" -> EventPageResponse.of(eventRepository.findAllByEventStatus(EventStatus.ACTIVE, pageable));
            case "ended" -> EventPageResponse.of(eventRepository.findAllByEventStatus(EventStatus.ENDED, pageable));
            default -> throw new TicketingException(TicketingErrorCode.EVENT_NOT_FOUND);
        };
    }

    private Event findEventById(Long eventId) {

        return eventRepository.findById(eventId)
                .orElseThrow(() -> new TicketingException(TicketingErrorCode.EVENT_NOT_FOUND));
    }

    private String findAdminUser() {
        String userId = userClientService
                .fetchUserIdAndUsername(SecurityUtil.getEmail())
                .userId();

        if (userId == null || userId.isBlank()) {
            throw new UserException(UserErrorCode.USER_VALIDATION_FAILED);
        }

        return userId;
    }

    private void validationEvent(Event event, String userId) {
        if (!event.getUserId().equals(userId) && !SecurityUtil.hasRole("ADMIN")) {
            throw new TicketingException(TicketingErrorCode.UNAUTHORIZED_EVENT_UPDATE);
        }

        if (event.getEventTime().isBefore(LocalDateTime.now())) {
            throw new TicketingException(TicketingErrorCode.EVENT_ALREADY_STARTED);
        }
    }

    private int getLastPage(int totalPage) {

        return totalPage > 0 ? totalPage - 1 : 0;
    }

    private int getNextPage(boolean hasNext, int page) {
        if (hasNext) {
            return page + 1;
        }

        return -1;
    }

    private Participation getParticipationByEventIdAndUserId(Long eventId, String userId) {

        return participationRepository.findByEvent_IdAndProfile_UserId(eventId, userId).orElseThrow(() -> new UserException(UserErrorCode.USER_VALIDATION_FAILED));
    }
}
