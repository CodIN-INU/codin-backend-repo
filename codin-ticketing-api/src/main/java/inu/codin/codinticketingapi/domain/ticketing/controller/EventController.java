package inu.codin.codinticketingapi.domain.ticketing.controller;

import inu.codin.codinticketingapi.common.response.SingleResponse;
import inu.codin.codinticketingapi.domain.ticketing.dto.stream.EventStockStream;
import inu.codin.codinticketingapi.domain.ticketing.entity.Campus;
import inu.codin.codinticketingapi.domain.ticketing.entity.ParticipationStatus;
import inu.codin.codinticketingapi.domain.ticketing.service.EventService;
import inu.codin.codinticketingapi.domain.ticketing.service.EventStockProducerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ticketing/event")
@RequiredArgsConstructor
@Tag(name = "Event API", description = "티켓팅 이벤트 API")
public class EventController {

    private final EventService eventService;

    private final EventStockProducerService eventStockProducerService;

    /** 티켓팅 이벤트 목록 조회 (송도캠, 미추홀캠) */
    @GetMapping
    public ResponseEntity<SingleResponse<?>> getEventList(
            @RequestParam @Valid Campus campus,
            @RequestParam("page") @NotNull int pageNumber
    ) {
        return ResponseEntity.ok(new SingleResponse<>(200, "티켓팅 이벤트 게시물 리스트 조회 성공",
                eventService.getEventList(campus, pageNumber)));
    }

    /** 티켓팅 이벤트 상세 정보 조회 */
    @GetMapping("/{eventId}")
    public ResponseEntity<SingleResponse<?>> getEventDetail(
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(new SingleResponse<>(200, "티켓팅 이벤트 상세 정보 조회 성공",
                eventService.getEventDetail(eventId)));
    }

    /** 유저 마이페이지 티켓팅 참여 전체 이력 조회 */
    @GetMapping("/user")
    public ResponseEntity<SingleResponse<?>> getUserEventList(
            @RequestParam("page") @NotNull int pageNumber
    ) {
        return ResponseEntity.ok(new SingleResponse<>(200, "티켓팅 유저 티켓팅 참여 전체 이력 조회",
                eventService.getUserEventList(pageNumber)));
    }

    /** 유저 마이페이지 티켓팅 참여 완료, 미수령, 취소 이력 조회 */
    @GetMapping("/user/status")
    public ResponseEntity<SingleResponse<?>> getUserEventListByStatus(
            @RequestParam("page") @NotNull int pageNumber,
            @RequestParam("status") ParticipationStatus status
    ) {
        return ResponseEntity.ok(new SingleResponse<>(200, "유저 티켓팅 참여 (완료, 취소) 이력 조회",
                eventService.getUserEventListByStatus(pageNumber, status)));
    }

    @PostMapping(value = "sse/{eventId}")
    @Operation(summary = "[테스트] 재고상태 SSE 전송")
    public ResponseEntity<?> sendQuantityUpdateEvent(
            @PathVariable Long eventId,
            @RequestParam Long quantity
    ) {
        eventStockProducerService.publishEventStock(new EventStockStream(eventId, quantity));
        return ResponseEntity.ok().build();
    }
}
