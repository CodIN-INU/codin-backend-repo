package inu.codin.codinticketingapi.domain.ticketing.service;

import inu.codin.codinticketingapi.domain.admin.entity.Event;
import inu.codin.codinticketingapi.domain.image.service.ImageService;
import inu.codin.codinticketingapi.domain.ticketing.dto.event.StockDecrementRequest;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.ParticipationCreateResponse;
import inu.codin.codinticketingapi.domain.ticketing.entity.Participation;
import inu.codin.codinticketingapi.domain.ticketing.entity.Profile;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingErrorCode;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingException;
import inu.codin.codinticketingapi.domain.ticketing.repository.EventRepository;
import inu.codin.codinticketingapi.domain.ticketing.repository.ParticipationRepository;
import inu.codin.codinticketingapi.domain.ticketing.repository.ProfileRepository;
import inu.codin.codinticketingapi.domain.user.service.UserClientService;
import inu.codin.codinticketingapi.security.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class TicketingService {

    private final EventRepository eventRepository;
    private final ProfileRepository profileRepository;
    private final ParticipationRepository participationRepository;

    private final ApplicationEventPublisher eventPublisher;
    private final UserClientService userClientService;
    private final ImageService imageService;

    @Transactional
    public ParticipationCreateResponse saveParticipation(Long eventId) {
        String userId = userClientService.fetchUser().getUserId();

        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new TicketingException(TicketingErrorCode.PROFILE_NOT_FOUND));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new TicketingException(TicketingErrorCode.EVENT_NOT_FOUND));

        // todo: int ticketNumber = Stock에서 티켓팅 번호 가져오기
        Participation participation = Participation.builder()
                .event(event)
                .ticketNumber(1)
                .profile(profile)
                .build();
        return ParticipationCreateResponse.of(participationRepository.save(participation));
    }

    @Transactional
    public void processParticipationSuccess(Long eventId, String adminPassword, MultipartFile signatureImage) {
        String userId = userClientService.fetchUser().getUserId();

        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new TicketingException(TicketingErrorCode.PROFILE_NOT_FOUND));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new TicketingException(TicketingErrorCode.EVENT_NOT_FOUND));

        // 비밀번호 확인
        if (!adminPassword.equals(event.getEventPassword())) {
            throw new TicketingException(TicketingErrorCode.PASSWORD_INVALID);
        }
        // 서명 이미지 업로드(MultipartFile) 및 url 저장
        String signatureImageUrl = imageService.handleImageUpload(signatureImage);

        // 참여자 정보 조회
        Participation participation = participationRepository.findByEventAndProfile(event, profile)
                .orElseThrow(() -> new TicketingException(TicketingErrorCode.PARTICIPATION_NOT_FOUND));

        // 수령 완료 상태로 변경
        participation.changeStatusCompleted();
        participation.setSignatureImgUrl(signatureImageUrl);
        participationRepository.save(participation);

        // 수령 정보 반영
        eventPublisher.publishEvent(new StockDecrementRequest(event));
    }

    @Transactional
    public void changeParticipationStatusCanceled(Long eventId) {
        String userId = userClientService.fetchUser().getUserId();

        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new TicketingException(TicketingErrorCode.PROFILE_NOT_FOUND));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new TicketingException(TicketingErrorCode.EVENT_NOT_FOUND));
        Participation participation = participationRepository.findByEventAndProfile(event, profile)
                .orElseThrow(() -> new TicketingException(TicketingErrorCode.PARTICIPATION_NOT_FOUND));

        participation.changeStatusCanceled();
        participationRepository.save(participation);
    }

    // todo: 특정 이벤트의 잔여수량 실시간 체크 기능 (STOMP?)
}
