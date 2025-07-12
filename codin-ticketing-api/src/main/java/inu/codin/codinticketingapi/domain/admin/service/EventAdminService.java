package inu.codin.codinticketingapi.domain.admin.service;

import inu.codin.codinticketingapi.domain.ticketing.dto.response.EventPageResponse;
import inu.codin.codinticketingapi.security.util.SecurityUtil;
import inu.codin.codinticketingapi.infra.redis.RedisEventService;
import inu.codin.codinticketingapi.domain.image.service.ImageService;
import inu.codin.codinticketingapi.domain.admin.dto.EventCreateRequest;
import inu.codin.codinticketingapi.domain.admin.dto.EventUpdateRequest;
import inu.codin.codinticketingapi.domain.admin.entity.Event;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingErrorCode;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingException;
import inu.codin.codinticketingapi.domain.ticketing.repository.EventRepository;
import inu.codin.codinticketingapi.domain.user.service.UserClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class EventAdminService {

    private final EventRepository eventRepository;
    private final UserClientService userClientService;

    private final ImageService imageService;
    private final RedisEventService redisEventService;
//    private final Clock clock;

    public EventPageResponse getEventListByManager(int pageNumber) {
        String userId = userClientService.fetchUserIdAndUsername(SecurityUtil.getEmail()).userId();
        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("createdAt").descending());
        return EventPageResponse.of(eventRepository.findByCreatedUserId(userId, pageable));
    }

    public String getEventPassword(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new TicketingException(TicketingErrorCode.EVENT_NOT_FOUND))
                .getEventPassword();
    }

    @Transactional
    public Event createEvent(EventCreateRequest request, MultipartFile eventImage) {
        String userId = userClientService.fetchUserIdAndUsername(SecurityUtil.getEmail()).userId();
        request.validateEventTimes();

        String eventImageUrl = imageService.handleImageUpload(eventImage);
        Event event = eventRepository.save(request.toEntity(userId, eventImageUrl));

        redisEventService.initializeEvent(event.getId(), event.getQuantity(), event.getEventEndTime());
        return event;
    }

    @Transactional
    public Event updateEvent(Long eventId, EventUpdateRequest request, MultipartFile eventImage) {
        // 엔티티 조회, 권한 검증
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new TicketingException(TicketingErrorCode.EVENT_NOT_FOUND));

        String currentUserId = userClientService
                .fetchUserIdAndUsername(SecurityUtil.getEmail())
                .userId();

        if (!event.getUserId().equals(currentUserId) && !SecurityUtil.hasRole("ADMIN")) {
            throw new TicketingException(TicketingErrorCode.UNAUTHORIZED_EVENT_UPDATE);
        } else if (event.getEventTime().isBefore(LocalDateTime.now())) {
            throw new TicketingException(TicketingErrorCode.EVENT_ALREADY_STARTED);
        }

        // 입력값 검증
        request.validateEventTimes();

        // 수량 변경 대비
        int oldQuantity = event.getQuantity();

        // 이미지 처리
        if (eventImage != null && !eventImage.isEmpty()) {
            String newUrl = imageService.handleImageUpload(eventImage);
            event.updateImageUrl(newUrl);
        }

        // 엔티티 업데이트
        event.updateFrom(request);
        Event saved = eventRepository.save(event);

        // Redis 동기화 - (수량 변경 시)
        int newQuantity = saved.getQuantity();
        if (newQuantity != oldQuantity) {
            redisEventService.initializeEvent(event.getId(), event.getQuantity(), event.getEventEndTime());
        }
        return saved;
    }

    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new TicketingException(TicketingErrorCode.EVENT_NOT_FOUND));
        event.delete();
        redisEventService.deleteEvent(eventId);
        eventRepository.save(event);
    }
}