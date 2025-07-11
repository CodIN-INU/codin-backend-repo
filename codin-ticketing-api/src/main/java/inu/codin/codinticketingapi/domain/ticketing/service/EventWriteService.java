package inu.codin.codinticketingapi.domain.ticketing.service;

import inu.codin.codinticketingapi.common.security.util.SecurityUtil;
import inu.codin.codinticketingapi.domain.redis.RedisEventService;
import inu.codin.codinticketingapi.domain.s3.service.ImageService;
import inu.codin.codinticketingapi.domain.ticketing.dto.request.EventCreateRequest;
import inu.codin.codinticketingapi.domain.ticketing.entity.TicketingEvent;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingErrorCode;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingException;
import inu.codin.codinticketingapi.domain.ticketing.repository.EventRepository;
import inu.codin.codinticketingapi.domain.user.service.UserClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class EventWriteService {

    private final EventRepository eventRepository;
    private final UserClientService userClientService;

    private final ImageService imageService;
    private final RedisEventService redisEventService;

    @Transactional
    public Long createEvent(EventCreateRequest request, MultipartFile eventImage) {
        String userId = userClientService.fetchUserIdAndUsername(SecurityUtil.getEmail()).userId();
        request.validateEventTimes();

        String eventImageUrl = imageService.handleImageUpload(eventImage);
        TicketingEvent event = eventRepository.save(request.toEntity(userId, eventImageUrl));
        setupEventInRedis(event);
        return event.getId();
    }

    public void updateEvent() {
        // 이벤트 수정 로직
        // 1. 이벤트 수정 유저 검증
        // 2. 수정된 이벤트 내용 검증
        // 3. 수정된 내용 중에서 Redis에 캐싱된 내용 Validation
    }

    public void deleteEvent(Long eventId) {
        TicketingEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new TicketingException(TicketingErrorCode.EVENT_NOT_FOUND));
        event.delete();
        eventRepository.save(event);
    }

    private void setupEventInRedis(TicketingEvent event) {
        redisEventService.setEventData(event.getId(), event.getQuantity());
    }
}