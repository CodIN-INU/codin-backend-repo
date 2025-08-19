package inu.codin.codinticketingapi.domain.ticketing.controller;

import inu.codin.codinticketingapi.common.response.SingleResponse;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.EventDetailResponse;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.EventPageResponse;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.EventParticipationHistoryPageResponse;
import inu.codin.codinticketingapi.domain.ticketing.dto.stream.EventStockStream;
import inu.codin.codinticketingapi.domain.ticketing.entity.Campus;
import inu.codin.codinticketingapi.domain.ticketing.entity.ParticipationStatus;
import inu.codin.codinticketingapi.domain.ticketing.service.EventService;
import inu.codin.codinticketingapi.domain.ticketing.service.EventStockProducerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/event")
@RequiredArgsConstructor
@Tag(name = "Event API", description = "티켓팅 이벤트 API")
public class EventController {

    private final EventService eventService;

    private final EventStockProducerService eventStockProducerService;

    /** 티켓팅 이벤트 목록 조회 (송도캠, 미추홀캠) */
    @GetMapping
    @Operation(summary = "티켓팅 이벤트 목록 조회 (송도 캠퍼스, 미추홀 캠퍼스)")
    @ApiResponse(responseCode = "200", description = "티켓팅 이벤트 게시물 리스트 조회 성공")
    public ResponseEntity<SingleResponse<EventPageResponse>> getEventList(
            @Parameter(description = "캠퍼스", example = "SONGDO_CAMPUS") @RequestParam Campus campus,
            @Parameter(description = "페이지", example = "0") @RequestParam("page") @NotNull int pageNumber
    ) {
        return ResponseEntity.ok(new SingleResponse<>(200, "티켓팅 이벤트 게시물 리스트 조회 성공",
                eventService.getEventList(campus, pageNumber)));
    }

    /** 티켓팅 이벤트 상세 정보 조회 */
    @GetMapping("/{eventId}")
    @Operation(summary = "티켓팅 이벤트 상세 정보 조회")
    @ApiResponse(responseCode = "200", description = "티켓팅 이벤트 상세 정보 조회 성공")
    public ResponseEntity<SingleResponse<EventDetailResponse>> getEventDetail(
            @Parameter(description = "이벤트 ID", example = "1111") @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(new SingleResponse<>(200, "티켓팅 이벤트 상세 정보 조회 성공",
                eventService.getEventDetail(eventId)));
    }

    /** 유저 마이페이지 티켓팅 참여 전체 이력 조회 */
    @GetMapping("/user")
    @Operation(summary = "유저 마이페이지 티켓팅 참여 전체 이력 조회")
    @ApiResponse(responseCode = "200", description = "티켓팅 유저 티켓팅 참여 전체 이력 조회 성공")
    public ResponseEntity<SingleResponse<EventParticipationHistoryPageResponse>> getUserEventList(
            @Parameter(description = "페이지", example = "0") @RequestParam("page") @NotNull int pageNumber
    ) {
        return ResponseEntity.ok(new SingleResponse<>(200, "티켓팅 유저 티켓팅 참여 전체 이력 조회",
                eventService.getUserEventList(pageNumber)));
    }

    /** 유저 마이페이지 티켓팅 참여 완료, 미수령, 취소 이력 조회 */
    @GetMapping("/user/status")
    @Operation(summary = "유저 마이페이지 티켓팅 참여 완료, 미수령, 취소 이력 조회 ")
    @ApiResponse(responseCode = "200", description = "유저 티켓팅 참여 상태별 이력 조회 성공")
    public ResponseEntity<SingleResponse<EventParticipationHistoryPageResponse>> getUserEventListByStatus(
            @Parameter(description = "페이지", example = "0") @RequestParam("page") @NotNull int pageNumber,
            @Parameter(description = "티켓팀 참여 상태", example = "COMPLETED") @RequestParam("status") ParticipationStatus status
    ) {
        return ResponseEntity.ok(new SingleResponse<>(200, "유저 티켓팅 참여 (완료, 취소) 이력 조회",
                eventService.getUserEventListByStatus(pageNumber, status)));
    }

    /** [테스트] 재고상태 구독자들에게 SSE 전송 */
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PostMapping(value = "sse/{eventId}")
    @Operation(summary = "[테스트] 재고상태 구독자들에게 SSE 전송 - MANAGER, ADMIN")
    @ApiResponse(responseCode = "200", description = "SSE 전송 성공")
    public ResponseEntity<?> sendQuantityUpdateEvent(
            @Parameter(description = "구독한 이벤트 ID", example = "1111") @PathVariable Long eventId,
            @Parameter(description = "전송할 임의 재고 상태", example = "100") @RequestParam Long quantity
    ) {
        eventStockProducerService.publishEventStock(new EventStockStream(eventId, quantity));
        return ResponseEntity.ok(new SingleResponse<>(200, "SSE 전송 성공", null));
    }
}
