package inu.codin.codinticketingapi.domain.ticketing.controller;

import inu.codin.codinticketingapi.common.response.SingleResponse;
import inu.codin.codinticketingapi.domain.ticketing.service.TicketingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ticketing")
@RequiredArgsConstructor
@Tag(name = "Ticketing API", description = "티켓팅 API")
public class TicketingController {

    private final TicketingService ticketingService;

    // todo: 사용자 티켓팅 잔여수량 (SSE, WebSocket..)
    // todo: 사용자 티켓팅 취소

    /** 특정 티켓팅 이벤트에 티켓팅 참여 (교환권 부여) */
    @PostMapping("/events/{eventId}/join")
    public SingleResponse<?> joinTicketingEvent(
            @PathVariable Long eventId
    ) {
        return new SingleResponse<>(200, "티켓팅 이벤트 참여 및 교환권 부여 성공",
                ticketingService.createUserParticipation(eventId));
    }

    // todo: 교환권을 부여받은 이후 관리자의 비밀번호를 통해 수령 확인
    @PostMapping("/events/{eventId}/confirm")
    public SingleResponse<?> confirmTicketingByAdmin(
            @PathVariable Long eventId,
            @RequestParam String adminPassword
    ) {
        // 실제 구현 필요
        // SecurityContextHolder를 통해 유저 데이터 가져옴 (SecurityUtil)
        return new SingleResponse<>(200, "관리자 비밀번호로 수령 확인 성공", null);
    }

    // todo: 서명을 통한 데이터를 사용자에게 받아와 S3 저장 및 처리
    @PostMapping("/events/{eventId}/signature")
    public SingleResponse<?> uploadSignature(
            @PathVariable Long eventId,
            @RequestParam String signatureImgUrl
    ) {
        // 실제 구현 필요
        // SecurityContextHolder를 통해 유저 데이터 가져옴 (SecurityUtil)
        return new SingleResponse<>(200, "서명 이미지 저장 및 처리 성공", null);
    }
}