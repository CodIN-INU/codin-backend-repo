package inu.codin.codinticketingapi.domain.admin.controller;

import inu.codin.codinticketingapi.common.response.SingleResponse;
import inu.codin.codinticketingapi.domain.admin.controller.swagger.EventAdminController;
import inu.codin.codinticketingapi.domain.admin.dto.request.EventCreateRequest;
import inu.codin.codinticketingapi.domain.admin.dto.request.EventUpdateRequest;
import inu.codin.codinticketingapi.domain.admin.dto.response.EventParticipationProfilePageResponse;
import inu.codin.codinticketingapi.domain.admin.dto.response.EventResponse;
import inu.codin.codinticketingapi.domain.admin.dto.response.EventStockResponse;
import inu.codin.codinticketingapi.domain.admin.service.EventAdminService;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.EventPageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/event")
@RequiredArgsConstructor
public class EventAdminControllerImpl implements EventAdminController {

    private final EventAdminService eventAdminService;

    /**
     * 티켓팅 이벤트 생성
     */
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SingleResponse<EventResponse>> createEvent(
            @RequestPart("eventContent") @Valid EventCreateRequest eventCreateRequest,
            @RequestPart(value = "eventImage") MultipartFile eventImage) {

        return ResponseEntity.ok(new SingleResponse<>(201, "티켓팅 이벤트 생성 성공",
                eventAdminService.createEvent(eventCreateRequest, eventImage)));
    }

    /**
     * 티켓팅 관리자 - 티켓팅 리스트 조회
     */
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<SingleResponse<EventPageResponse>> getEventListByManager(@RequestParam String status, @RequestParam("page") @NotNull int pageNumber) {

        return ResponseEntity.ok(new SingleResponse<>(200, "[티켓팅 관리자] 이벤트 게시물 리스트 반환 성공",
                eventAdminService.getEventListByManager(status, pageNumber)));
    }

    /**
     * 티켓팅 이벤트 수정
     */
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PutMapping(value = "/{eventId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SingleResponse<EventResponse>> updateEvent(
            @PathVariable Long eventId,
            @RequestBody EventUpdateRequest eventUpdateRequest,
            @RequestPart(value = "eventImage", required = false) MultipartFile eventImage) {

        return ResponseEntity.ok(new SingleResponse<>(200, "티켓팅 이벤트 수정 성공",
                eventAdminService.updateEvent(eventId, eventUpdateRequest, eventImage)));
    }

    /**
     * 티켓팅 이벤트 삭제
     */
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @DeleteMapping("/{eventId}")
    public ResponseEntity<SingleResponse<Boolean>> deleteEvent(@PathVariable Long eventId) {
        eventAdminService.deleteEvent(eventId);

        return ResponseEntity.ok(new SingleResponse<>(200, "티켓팅 이벤트 삭제 성공", true));
    }

    /**
     * 티켓팅 이벤트 비밀번호 탐색
     */
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @GetMapping("/{eventId}/password")
    public ResponseEntity<SingleResponse<String>> getEventPassword(@PathVariable Long eventId) {

        return ResponseEntity.ok(new SingleResponse<>(200, "티켓팅 이벤트 비밀번호 반환 성공",
                eventAdminService.getEventPassword(eventId)));
    }

    /**
     * 티켓팅 마감
     */
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PostMapping("/{eventId}/close")
    public ResponseEntity<SingleResponse<Boolean>> closeEvent(@PathVariable Long eventId) {
        eventAdminService.closeEvent(eventId);

        return ResponseEntity.ok(new SingleResponse<>(200, "티켓팅 이벤트 마감 성공", true));
    }

    /**
     * 수령자 리스트 조회
     */
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @GetMapping("/{eventId}/participation")
    public ResponseEntity<SingleResponse<EventParticipationProfilePageResponse>> getEventPart(@PathVariable Long eventId, @RequestParam("page") @NotNull int pageNumber) {

        return ResponseEntity.ok(new SingleResponse<>(200, "이벤트 참여 인원 반환 성공",
                eventAdminService.getParticipationList(eventId, pageNumber)));
    }

    /**
     * 관리자가 수령 상태 변경 기능
     */
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PutMapping("{eventId}/management/status/{userId}")
    public ResponseEntity<SingleResponse<Boolean>> changeReceiveStatus(@PathVariable Long eventId, @PathVariable String userId) {

        return ResponseEntity.ok(new SingleResponse<>(200, "수령완료 변경 성공", eventAdminService.changeReceiveStatus(eventId, userId)));
    }

    /**
     * 상품 잔여 수량 업데이트
     */
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @GetMapping("{eventId}/management/stock")
    public ResponseEntity<SingleResponse<EventStockResponse>> getStock(@PathVariable Long eventId) {

        return ResponseEntity.ok(new SingleResponse<>(200, "티켓 및 상품 잔려 수령 반환 성공", eventAdminService.getStock(eventId)));
    }

    /**
     * 관리자가 사용자 티켓팅 취소
     */
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @DeleteMapping("{eventId}/management/cancel/{userId}")
    public ResponseEntity<SingleResponse<Boolean>> cancelTicket(@PathVariable Long eventId, @PathVariable String userId) {

        return ResponseEntity.ok(new SingleResponse<>(200, "사용자 티켓 취소 성공", eventAdminService.cancelTicket(eventId, userId)));
    }

    /**
     * 관리자가 이벤트 수동 오픈
     */
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PostMapping("/{eventId}/open")
    public ResponseEntity<SingleResponse<Boolean>> openEvent(@PathVariable Long eventId) {

        return ResponseEntity.ok(new SingleResponse<>(200, "이벤트 수동 오픈 성공", eventAdminService.openEvent(eventId)));
    }
}
