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
import inu.codin.codinticketingapi.domain.ticketing.entity.ParticipationStatus;
import inu.codin.codinticketingapi.domain.ticketing.entity.Stock;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingErrorCode;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingException;
import inu.codin.codinticketingapi.domain.ticketing.redis.RedisEventService;
import inu.codin.codinticketingapi.domain.ticketing.repository.EventRepository;
import inu.codin.codinticketingapi.domain.ticketing.repository.ParticipationRepository;
import inu.codin.codinticketingapi.domain.user.exception.UserErrorCode;
import inu.codin.codinticketingapi.domain.user.exception.UserException;
import inu.codin.codinticketingapi.domain.user.service.UserClientService;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventAdminService {

    private final EventRepository eventRepository;
    private final ParticipationRepository participationRepository;

    private static final int PAGE_SIZE = 10;

    private final ImageService imageService;
    private final RedisEventService redisEventService;
    private final UserClientService userClientService;
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
        eventStatusScheduler.scheduleCreateOrUpdatedEvent(savedEvent);
        redisEventService.initializeTickets(savedEvent.getId(), stock.getCurrentTotalStock());

        return EventResponse.of(savedEvent);
    }

    public EventPageResponse eventPageResponseWithStatus(String status, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, PAGE_SIZE, Sort.by("createdAt").descending());
        Page<Event> eventPage = findEventsByStatus(status, pageable);
        Map<Long, Long> waitingCountMap = getWaitingCountMap(eventPage);

        return EventPageResponse.from(eventPage, waitingCountMap);
    }

    @Transactional
    public EventResponse updateEvent(Long eventId, EventUpdateRequest request, MultipartFile eventImage) {
        // 엔티티 조회, 권한 검증
        Event findEvent = findEventById(eventId);
        String currentUserId = findAdminUser();
        int prevStock = findEvent.getStock().getCurrentTotalStock();

        // 입력값 검증
        request.validateEventTimes();
        validationEvent(findEvent, currentUserId);

        // 이미지 처리
        if (eventImage != null && !eventImage.isEmpty()) {
            String newUrl = imageService.handleImageUpload(eventImage);
            findEvent.updateImageUrl(newUrl);
        }

        // 엔티티 업데이트
        findEvent.updateFrom(request);

        eventStatusScheduler.scheduleCreateOrUpdatedEvent(findEvent);
        redisEventService.updateTickets(findEvent.getId(), findEvent.getStock().getCurrentTotalStock(), prevStock);

        return EventResponse.of(findEvent);
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = findEventById(eventId);
        event.delete();

        eventStatusScheduler.scheduleAllDelete(event);
        redisEventService.deleteTickets(eventId);
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
        eventStatusScheduler.scheduleAllDelete(findEvent);
        redisEventService.deleteTickets(eventId);
    }

    @Transactional(readOnly = true)
    public EventParticipationProfilePageResponse getParticipationList(Long eventId, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, 10, Sort.by("ticketNumber").descending());

        Event event = findEventById(eventId);
        Stock stock = event.getStock();
        Page<Participation> participationList = participationRepository.findAllByEvent_Id(eventId, pageable);
        List<EventParticipationProfileResponse> profileList = participationList.stream()
                .map(EventParticipationProfileResponse::of)
                .toList();

        int lastPage = getLastPage(participationList.getTotalPages());
        int nextPage = getNextPage(participationList.hasNext(), participationList.getNumber());
        int waitCount = participationRepository.countByEvent_IdAndStatus(eventId, ParticipationStatus.WAITING);

        return new EventParticipationProfilePageResponse(profileList, lastPage, nextPage, event.getTitle(), stock.getRemainingStock(), waitCount, event.getEventEndTime());
    }

    @Transactional
    public boolean changeReceiveStatus(Long eventId, String userId, MultipartFile image) {
        Participation findParticipation = getParticipationByEventIdAndUserId(eventId, userId);
        String imageURL = imageService.handleImageUpload(image);

        findParticipation.changeStatusCompleted(imageURL);

        return true;
    }

    @Transactional(readOnly = true)
    public EventStockResponse getStock(Long eventId) {
        Event findEvent = findEventById(eventId);
        Stock stock = findEvent.getStock();

        return EventStockResponse.of(stock.getRemainingStock());
    }

    @Transactional
    public Boolean cancelTicket(Long eventId, String userId) {
        Participation findParticipation = getParticipationByEventIdAndUserId(eventId, userId);
        findParticipation.changeStatusCanceled();

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

    private Page<Event> findEventsByStatus(String status, Pageable pageable) {
        return switch (status) {
            case "all" -> eventRepository.findAll(pageable);
            case "upcoming" -> eventRepository.findAllByEventStatusAndDeletedAtIsNull(EventStatus.UPCOMING, pageable);
            case "open" -> eventRepository.findAllByEventStatusAndDeletedAtIsNull(EventStatus.ACTIVE, pageable);
            case "ended" -> eventRepository.findAllByEventStatusAndDeletedAtIsNull(EventStatus.ENDED, pageable);
            default -> throw new TicketingException(TicketingErrorCode.EVENT_NOT_FOUND);
        };
    }

    private Event findEventById(Long eventId) {

        return eventRepository.findById(eventId)
                .orElseThrow(() -> new TicketingException(TicketingErrorCode.EVENT_NOT_FOUND));
    }

    private String findAdminUser() {
        String userId = userClientService
                .fetchUser()
                .getUserId();

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

        return participationRepository.findByEvent_IdAndUserId(eventId, userId).orElseThrow(() -> new UserException(UserErrorCode.USER_VALIDATION_FAILED));
    }

    private Map<Long, Long> getWaitingCountMap(Page<Event> eventPage) {
        List<Long> eventIds = eventPage.stream()
                .map(Event::getId)
                .toList();

        if (eventIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return participationRepository
                .countWaitingByEventIds(ParticipationStatus.WAITING, eventIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }
}
