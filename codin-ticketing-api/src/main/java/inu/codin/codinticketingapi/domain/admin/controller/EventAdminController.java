package inu.codin.codinticketingapi.domain.admin.controller;

import inu.codin.codinticketingapi.common.response.SingleResponse;
import inu.codin.codinticketingapi.domain.admin.service.EventAdminService;
import inu.codin.codinticketingapi.domain.admin.dto.EventCreateRequest;
import inu.codin.codinticketingapi.domain.admin.dto.EventUpdateRequest;
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
@RequestMapping("/ticketing/event")
@RequiredArgsConstructor
@Tag(name = "Event API", description = "티켓팅 이벤트 API")
public class EventAdminController {

    private final EventAdminService eventAdminService;

    // todo: 수령자 관리에서 서명보기 기능
    // todo: 관리자가 수동으로 수령완료 변경 기능
    // todo: 관리자 티켓팅 잔여수량, 수령대기
    // todo: 관리자가 사용자 티켓팅 취소

    /** 티켓팅 관리자 - 티켓팅 리스트 조회 */
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @GetMapping("/management")
    public ResponseEntity<SingleResponse<?>> getEventListByManager(
            @RequestParam("page") @NotNull int pageNumber
    ) {
        return ResponseEntity.ok(new SingleResponse<>(200, "[티켓팅 관리자] 이벤트 게시물 리스트 반환 성공",
                eventAdminService.getEventListByManager(pageNumber)));
    }

    /** 티켓팅 이벤트 비밀번호 탐색 */
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @GetMapping("/{eventId}/password")
    public ResponseEntity<SingleResponse<?>> getEventPassword(
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(new SingleResponse<>(200, "티켓팅 이벤트 비밀번호 반환 성공",
                eventAdminService.getEventPassword(eventId)));
    }

    // todo: 티켓팅 이벤트 마감 - 마감시 더이상 유저(트래픽)를 받지 않음
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PostMapping("/{eventId}/close")
    public ResponseEntity<SingleResponse<?>> closeEvent(
            @PathVariable String eventId
    ) {
        // eventAdminService.closeEvent(eventId);
        return ResponseEntity.ok(new SingleResponse<>(200, "티켓팅 이벤트 마감 성공", null));
    }

    /** 티켓팅 이벤트 생성 */
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SingleResponse<?>> createEvent(
            @RequestPart("eventContent") @Valid EventCreateRequest eventCreateRequest,
            @RequestPart(value = "eventImage") MultipartFile eventImage
    ) {
        return ResponseEntity.ok(new SingleResponse<>(200, "티켓팅 이벤트 생성 성공",
                eventAdminService.createEvent(eventCreateRequest, eventImage)));
    }

    /** 티켓팅 이벤트 수정 */
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PutMapping(value = "/{eventId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SingleResponse<?>> updateEvent(
            @PathVariable Long eventId,
            @RequestBody EventUpdateRequest eventUpdateRequest,
            @RequestPart(value = "eventImage", required = false) MultipartFile eventImage
    ) {
        return ResponseEntity.ok(new SingleResponse<>(200, "티켓팅 이벤트 수정 성공",
                eventAdminService.updateEvent(eventId, eventUpdateRequest, eventImage)));
    }

    /** 티켓팅 이벤트 삭제 */
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @DeleteMapping("/{eventId}")
    public ResponseEntity<SingleResponse<?>> deleteEvent(
            @PathVariable Long eventId
    ) {
        eventAdminService.deleteEvent(eventId);
        return ResponseEntity.ok(new SingleResponse<>(200, "티켓팅 이벤트 삭제 성공", null));
    }
}
