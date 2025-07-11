package inu.codin.codinticketingapi.domain.ticketing.controller;

import inu.codin.codinticketingapi.common.response.SingleResponse;
import inu.codin.codinticketingapi.domain.ticketing.dto.request.EventCreateRequest;
import inu.codin.codinticketingapi.domain.ticketing.entity.Campus;
import inu.codin.codinticketingapi.domain.ticketing.service.EventReadService;
import inu.codin.codinticketingapi.domain.ticketing.service.EventWriteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/ticketing/event")
@RequiredArgsConstructor
@Tag(name = "Event API", description = "티켓팅 이벤트 API")
public class EventController {

    private final EventReadService eventReadService;
    private final EventWriteService eventWriteService;

    /** 티켓팅 이벤트 목록 조회 (송도캠, 미추홀캠) */
    @GetMapping
    public ResponseEntity<SingleResponse<?>> getEventList(
            @RequestParam @Valid Campus campus,
            @RequestParam("page") @NotNull int pageNumber
    ) {
        return ResponseEntity.ok(new SingleResponse<>(200, "티켓팅 이벤트 게시물 리스트 조회 성공",
                eventReadService.getEventList(campus, pageNumber)));
    }

    /** 티켓팅 이벤트 상세 정보 조회 */
    @GetMapping("/{eventId}")
    public ResponseEntity<SingleResponse<?>> getEventDetail(
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(new SingleResponse<>(200, "티켓팅 이벤트 상세 정보 조회 성공",
                eventReadService.getEventDetail(eventId)));
    }

    /** 유저 마이페이지 티켓팅 참여 전체 이력 조회 */
    @GetMapping("/user")
    public ResponseEntity<SingleResponse<?>> getUserEventList(
            @RequestParam("page") @NotNull int pageNumber
    ) {
        return ResponseEntity.ok(new SingleResponse<>(200, "티켓팅 유저 티켓팅 참여 전체 이력 조회",
                eventReadService.getUserEventList(pageNumber)));
    }

    /** 유저 마이페이지 티켓팅 참여 완료, 취소 이력 조회 */
    @GetMapping("/user/canceled")
    public ResponseEntity<SingleResponse<?>> getUserEventListByCanceled(
            @RequestParam("page") @NotNull int pageNumber,
            @RequestParam("canceled") boolean canceled
    ) {
        return ResponseEntity.ok(new SingleResponse<>(200, "유저 티켓팅 참여 (완료, 취소) 이력 조회",
                eventReadService.getUserEventListByCanceled(pageNumber, canceled)));
    }

    /** 티켓팅 관리자 - 티켓팅 리스트 조회 */
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @GetMapping("/management")
    public ResponseEntity<SingleResponse<?>> getEventListByManager(
            @RequestParam("page") @NotNull int pageNumber
    ) {
        return ResponseEntity.ok(new SingleResponse<>(200, "[티켓팅 관리자] 이벤트 게시물 리스트 반환 성공",
                eventReadService.getEventListByManager(pageNumber)));
    }

    /** 티켓팅 이벤트 비밀번호 탐색 */
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @GetMapping("/{eventId}/password")
    public ResponseEntity<SingleResponse<?>> getEventPassword(
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(new SingleResponse<>(200, "티켓팅 이벤트 비밀번호 반환 성공",
                eventReadService.getEventPassword(eventId)));
    }

    // todo: 티켓팅 이벤트 마감 - 마감시 더이상 유저(트래픽)를 받지 않음
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PostMapping("/{eventId}/close")
    public ResponseEntity<SingleResponse<?>> closeEvent(
            @PathVariable String eventId
    ) {
        // eventWriteService.closeEvent(eventId);
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
                eventWriteService.createEvent(eventCreateRequest, eventImage)));
    }

    // todo: 티켓팅 이벤트 삭제
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @DeleteMapping("/{eventId}")
    public ResponseEntity<SingleResponse<?>> deleteEvent(
            @PathVariable String eventId
    ) {
        //eventWriteService.deleteEvent(eventId);
        return ResponseEntity.ok(new SingleResponse<>(200, "티켓팅 이벤트 삭제 성공", null));
    }

    // todo: 티켓팅 이벤트 수정
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PutMapping("/{eventId}")
    public ResponseEntity<SingleResponse<?>> updateEvent(
            @PathVariable String eventId
            /*, @RequestBody EventUpdateRequestDto dto */
    ) {
        // eventWriteService.updateEvent(eventId, dto);
        return ResponseEntity.ok(new SingleResponse<>(200, "티켓팅 이벤트 수정 성공", null));
    }
}
