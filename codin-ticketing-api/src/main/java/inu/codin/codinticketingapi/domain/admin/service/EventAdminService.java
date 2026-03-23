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
import inu.codin.codinticketingapi.domain.ticketing.service.TicketingService;
import inu.codin.codinticketingapi.domain.user.dto.UserInfoResponse;
import inu.codin.codinticketingapi.domain.user.exception.UserErrorCode;
import inu.codin.codinticketingapi.domain.user.exception.UserException;
import inu.codin.codinticketingapi.domain.user.service.UserClientService;

import inu.codin.common.entity.College;
import inu.codin.common.entity.Department;
import inu.codin.security.entity.UserRole;
import inu.codin.security.util.SecurityUtil;
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
    private final TicketingService ticketingService;

    @Transactional
    public EventResponse createEvent(EventCreateRequest request, MultipartFile eventImage) {
        UserInfoResponse userInfoResponse = userClientService.fetchUser();
        request.validateEventTimes();

        // 단과대 계정 또는 학과 계정인지 검증 후, 해당하는 데이터만 save
        Department department = null;
        College college = null;
        if (userInfoResponse.getDepartment() != null) {
            department = userInfoResponse.getDepartment();
        } else {
            college = userInfoResponse.getCollege();
        }

        // 정상적인 계정 (단과대 계정 또는 학과 계정)인지 검증
        if (userInfoResponse.getDepartment() == null && userInfoResponse.getCollege() == null) {
            throw new TicketingException(TicketingErrorCode.USER_INFO_INCOMPLETE);
        }

        String eventImageUrl = imageService.handleImageUpload(eventImage);
        Event event = request.toEntity(
                userInfoResponse.getUserId(),
                eventImageUrl,
                college,
                department
                );
        Stock stock = Stock.builder()
                .event(event)
                .initialStock(request.getQuantity())
                .build();

        Event savedEvent = eventRepository.save(event);
        eventStatusScheduler.scheduleCreateOrUpdatedEvent(savedEvent);
        redisEventService.initializeTickets(savedEvent.getId(), stock.getCurrentTotalStock());

        return EventResponse.from(savedEvent);
    }

    public EventPageResponse eventPageResponseWithStatus(String status, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, PAGE_SIZE, Sort.by("createdAt").descending());

        String userId = findAdminUser();
        boolean isAdmin = SecurityUtil.hasRole(UserRole.ADMIN);
        boolean isManager = SecurityUtil.hasRole(UserRole.MANAGER);

        Page<Event> eventPage = findEventsByStatus(status, userId, isAdmin, isManager, pageable);
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

        return EventResponse.from(findEvent);
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = findEventById(eventId);

        String currentUserId = findAdminUser();
        validationEvent(event, currentUserId);

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

        return EventParticipationProfilePageResponse.from(event, stock, profileList, lastPage, nextPage, waitCount);
    }

    @Transactional
    public boolean changeReceiveStatus(Long eventId, String userId, MultipartFile image) {
        // 권한 검증
        Event event = findEventById(eventId);
        String currentUserId = findAdminUser();
        validationEvent(event, currentUserId);

        Participation findParticipation = getParticipationByEventIdAndUserId(eventId, userId);
        String imageURL = imageService.handleImageUpload(image);

        findParticipation.changeStatusCompleted(imageURL);

        return true;
    }

    @Transactional(readOnly = true)
    public EventStockResponse getStock(Long eventId) {
        Event findEvent = findEventById(eventId);
        Stock stock = findEvent.getStock();

        return EventStockResponse.from(stock.getRemainingStock());
    }

    @Transactional
    public void cancelTicket(Long eventId, String userId) {
        // 권한 검증
        Event event = findEventById(eventId);
        String currentUserId = findAdminUser();
        validationEvent(event, currentUserId);

        ticketingService.cancelParticipation(eventId, userId);
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

    private Page<Event> findEventsByStatus(String status, String userId, Boolean isAdmin, Boolean isManager, Pageable pageable) {
        return switch (status) {
            case "all" -> eventRepository.findAll(userId, isAdmin, isManager, pageable);
            case "upcoming" -> eventRepository.findAllByEventStatusAndDeletedAtIsNull(EventStatus.UPCOMING, userId, isAdmin, isManager, pageable);
            case "open" -> eventRepository.findAllByEventStatusAndDeletedAtIsNull(EventStatus.ACTIVE, userId, isAdmin, isManager, pageable);
            case "ended" -> eventRepository.findAllByEventStatusAndDeletedAtIsNull(EventStatus.ENDED, userId, isAdmin, isManager, pageable);
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
        boolean isAdmin = SecurityUtil.hasRole(UserRole.ADMIN);
        boolean isManager = SecurityUtil.hasRole(UserRole.MANAGER);
        boolean isOwner = event.getUserId().equals(userId);

        // 관리자이거나 매니저이면서 이벤트 생성자일 경우에만 수정 가능
        if (!(isAdmin || (isManager && isOwner))) {
            throw new TicketingException(TicketingErrorCode.UNAUTHORIZED_EVENT_UPDATE);
        }

        if (!event.getEventStatus().equals(EventStatus.ENDED) && event.getEventTime().isBefore(LocalDateTime.now())) {
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
