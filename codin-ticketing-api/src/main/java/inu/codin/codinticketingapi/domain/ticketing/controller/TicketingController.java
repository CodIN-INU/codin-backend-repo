package inu.codin.codinticketingapi.domain.ticketing.controller;

import inu.codin.codinticketingapi.common.response.SingleResponse;
import inu.codin.codinticketingapi.domain.ticketing.service.TicketingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/ticketing")
@RequiredArgsConstructor
@Tag(name = "Ticketing API", description = "티켓팅 API")
public class TicketingController {

    private final TicketingService ticketingService;

    // todo: 사용자 티켓팅 잔여수량 (SSE, WebSocket..)

    /** 특정 티켓팅 이벤트에 티켓팅 참여 (교환권 부여) */
    @PostMapping("/events/join/{eventId}")
    public SingleResponse<?> createUserParticipation(
            @PathVariable Long eventId
    ) {
        return new SingleResponse<>(200, "티켓팅 이벤트 참여 및 교환권 부여 성공",
                ticketingService.saveParticipation(eventId));
    }

    /** 교환권을 부여받은 이후 관리자의 비밀번호를 통해 수령 확인, 서명 이미지 저장 */
    @PostMapping(value = "/events/complete/{eventId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SingleResponse<?> updateParticipationStatusByPassword(
            @PathVariable Long eventId,
            @RequestPart("password") String adminPassword,
            @RequestPart("signatureImage") MultipartFile signatureImage
    ) {
        ticketingService.processParticipationSuccess(eventId, adminPassword, signatureImage);
        return new SingleResponse<>(200, "관리자 비밀번호로 수령 확인 성공", null);
    }

    /** 사용자 티켓팅 취소 */
    @DeleteMapping(value = "/events/cancele/{eventId}")
    public SingleResponse<?> updateStatusCanceledParticipation(
            @PathVariable Long eventId
    ) {
        ticketingService.changeParticipationStatusCanceled(eventId);
        return new SingleResponse<>(200, "티켓팅 취소 완료", null);
    }
}