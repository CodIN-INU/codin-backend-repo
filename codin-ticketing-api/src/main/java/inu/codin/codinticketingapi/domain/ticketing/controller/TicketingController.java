package inu.codin.codinticketingapi.domain.ticketing.controller;

import inu.codin.codinticketingapi.common.response.SingleResponse;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.ParticipationResponse;
import inu.codin.codinticketingapi.domain.ticketing.service.ParticipationService;
import inu.codin.codinticketingapi.domain.ticketing.service.TicketingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/event")
@Tag(name = "Ticketing API", description = "티켓팅 API")
public class TicketingController {

    private final TicketingService ticketingService;
    private final ParticipationService participationService;

    /** 특정 티켓팅 이벤트의 참여 상태(교환권) 조회 */
    @GetMapping("/participation/{eventId}")
    @Operation(summary = "특정 티켓팅 이벤트의 참여 상태(교환권) 조회")
    @ApiResponse(responseCode = "200", description = "티켓팅 이벤트 참여 상태 조회 성공")
    public ResponseEntity<SingleResponse<ParticipationResponse>> findParticipationByEvent(
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(new SingleResponse<>(200, "티켓팅 이벤트 참여 상태 조회 성공",
                participationService.findParticipationByEvent(eventId)));
    }


    /** 유저의 특정 이벤트 티켓팅 단순 참여 상태(Boolean) 반환 */
    @GetMapping("/participation/check/{eventId}")
    @Operation(summary = "유저의 특정 이벤트 티켓팅 단순 참여 상태(Boolean) 반환 - (참여, 서명 상태 -> true / 미참여, 취소 -> false)")
    @ApiResponse(responseCode = "200", description = "티켓팅 이벤트 단순 참여 상태 조회 성공")
    public ResponseEntity<SingleResponse<Boolean>> readParticipationByEvent(
            @PathVariable Long eventId
    ) {
        participationService.findParticipationByEvent(eventId);
        return ResponseEntity.ok(new SingleResponse<>(200, "티켓팅 이벤트 단순 참여상태 조회 성공",
                participationService.isUserParticipatedInEvent(eventId)));
    }

    /** 특정 티켓팅 이벤트에 티켓팅 참여 (교환권 부여) */
    @PostMapping("/join/{eventId}")
    @Operation(summary = "특정 티켓팅 이벤트에 티켓팅 참여 (교환권 부여)")
    @ApiResponse(responseCode = "200", description = "티켓팅 이벤트 참여 및 교환권 부여 성공")
    public ResponseEntity<SingleResponse<ParticipationResponse>> createUserParticipation(
            @Parameter(description = "이벤트 ID", example = "1111") @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(new SingleResponse<>(200, "티켓팅 이벤트 참여 및 교환권 부여 성공",
                participationService.saveParticipation(eventId)));
    }

    /** 교환권을 부여받은 이후 관리자의 비밀번호를 통해 수령 확인, 서명 이미지 저장 */
    @PostMapping(value = "/complete/{eventId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "교환권을 부여받은 이후 관리자의 비밀번호를 통해 수령 확인, 서명 이미지 저장")
    @ApiResponse(responseCode = "200", description = "관리자 비밀번호로 수령 확인 성공")
    public ResponseEntity<SingleResponse<?>> updateParticipationStatusByPassword(
            @Parameter(description = "이벤트 ID", example = "1111") @PathVariable Long eventId,
            @Parameter(description = "관리자 비밀번호 (4자리)", example = "1234") @RequestPart("password") String adminPassword,
            @Parameter(description = "서명 이미지 파일") @RequestPart("signatureImage") MultipartFile signatureImage
    ) {
        ticketingService.processParticipationSuccess(eventId, adminPassword, signatureImage);
        return ResponseEntity.ok(new SingleResponse<>(200, "관리자 비밀번호로 수령 확인 성공", null));
    }

    /** 사용자 티켓팅 취소 */
    @DeleteMapping(value = "/cancel/{eventId}")
    @Operation(summary = "사용자 티켓팅 취소")
    @ApiResponse(responseCode = "200", description = "티켓팅 취소 완료")
    public ResponseEntity<SingleResponse<?>> updateStatusCanceledParticipation(
            @Parameter(description = "이벤트 ID", example = "1111") @PathVariable Long eventId
    ) {
        ticketingService.changeParticipationStatusCanceled(eventId);
        return ResponseEntity.ok(new SingleResponse<>(200, "티켓팅 취소 완료", null));
    }
}