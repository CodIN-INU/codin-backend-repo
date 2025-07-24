package inu.codin.codinticketingapi.domain.ticketing.service;

import inu.codin.codinticketingapi.domain.admin.entity.Event;
import inu.codin.codinticketingapi.domain.admin.entity.EventStatus;
import inu.codin.codinticketingapi.domain.image.service.ImageService;
import inu.codin.codinticketingapi.domain.ticketing.entity.Participation;
import inu.codin.codinticketingapi.domain.ticketing.entity.ParticipationStatus;
import inu.codin.codinticketingapi.domain.ticketing.entity.Stock;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingErrorCode;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingException;
import inu.codin.codinticketingapi.domain.ticketing.repository.EventRepository;
import inu.codin.codinticketingapi.domain.ticketing.repository.ParticipationRepository;
import inu.codin.codinticketingapi.domain.ticketing.repository.StockRepository;
import inu.codin.codinticketingapi.domain.user.service.UserClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class TicketingService {

    private final EventRepository eventRepository;
    private final ParticipationRepository participationRepository;
    private final StockRepository stockRepository;

    private final UserClientService userClientService;
    private final ImageService imageService;

    @Transactional
    public Stock decrement(Long eventId) {
        Stock stock = stockRepository.findByEvent_Id(eventId)
                .orElseThrow(() -> new TicketingException(TicketingErrorCode.STOCK_NOT_FOUND));
        // 재고 수량 감소
        if (!stock.decrease()) {
            throw new TicketingException(TicketingErrorCode.SOLD_OUT);
        }

        return stock;
    }

    /**
     * 티켓팅 이벤트 유저 참여 상태를 수령으로 변경
     * @param eventId 유저가 참여한 이벤트
     * @param adminPassword 수령 상태 확인을 위한 관리자 비밀번호
     * @param signatureImage 수령 확인 서명 이미지 파일
     */
    @Transactional
    public void processParticipationSuccess(Long eventId, String adminPassword, MultipartFile signatureImage) {
        String userId = userClientService.fetchUser().getUserId();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new TicketingException(TicketingErrorCode.EVENT_NOT_FOUND));
        // 이벤트 활동 상태 검증
        if (!event.getEventStatus().equals(EventStatus.ACTIVE)) {
            throw new TicketingException(TicketingErrorCode.EVENT_NOT_ACTIVE);
        }

        // 비밀번호 확인
        if (!adminPassword.equals(event.getEventPassword())) {
            throw new TicketingException(TicketingErrorCode.PASSWORD_INVALID);
        }
        // 서명 이미지 업로드(MultipartFile) 및 url 저장
        String signatureImageUrl = imageService.handleImageUpload(signatureImage);

        // 참여자 정보 조회
        Participation participation = participationRepository.findByEventAndUserId(event, userId)
                .orElseThrow(() -> new TicketingException(TicketingErrorCode.PARTICIPATION_NOT_FOUND));

        // 수령 완료 상태로 변경
        if (!participation.getStatus().equals(ParticipationStatus.WAITING)) {
            throw new TicketingException(TicketingErrorCode.CANNOT_CHANGE_STATUS);
        }
        participation.changeStatusCompleted();
        participation.setSignatureImgUrl(signatureImageUrl);
    }

    /**
     * 티켓팅 이벤트 유저 참여 상태를 취소로 변경
     * @param eventId 유저가 참여한 이벤트
     */
    @Transactional
    public void changeParticipationStatusCanceled(Long eventId) {
        String userId = userClientService.fetchUser().getUserId();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new TicketingException(TicketingErrorCode.EVENT_NOT_FOUND));
        // 이벤트 활동 상태 검증
        if (!event.getEventStatus().equals(EventStatus.ACTIVE)) {
            throw new TicketingException(TicketingErrorCode.EVENT_NOT_ACTIVE);
        }

        Participation participation = participationRepository.findByEventAndUserId(event, userId)
                .orElseThrow(() -> new TicketingException(TicketingErrorCode.PARTICIPATION_NOT_FOUND));

        if (!participation.getStatus().equals(ParticipationStatus.WAITING)) {
            throw new TicketingException(TicketingErrorCode.CANNOT_CHANGE_STATUS);
        }

        // 재고 증가
        Stock stock = stockRepository.findByEvent(event)
                .orElseThrow(() -> new TicketingException(TicketingErrorCode.STOCK_NOT_FOUND));
        stock.increase();
        stockRepository.save(stock); // todo: 경쟁 상태, Retryable 로직

        participation.changeStatusCanceled();
    }
}
