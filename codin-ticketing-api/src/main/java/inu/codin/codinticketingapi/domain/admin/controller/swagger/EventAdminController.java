package inu.codin.codinticketingapi.domain.admin.controller.swagger;

import inu.codin.codinticketingapi.common.response.SingleResponse;
import inu.codin.codinticketingapi.domain.admin.dto.request.EventCreateRequest;
import inu.codin.codinticketingapi.domain.admin.dto.request.EventUpdateRequest;
import inu.codin.codinticketingapi.domain.admin.dto.response.EventParticipationProfilePageResponse;
import inu.codin.codinticketingapi.domain.admin.dto.response.EventResponse;
import inu.codin.codinticketingapi.domain.admin.dto.response.EventStockResponse;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.EventPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Admin API", description = "관리자 권한을 가진 사용자들을 위한 이벤트 관리 API")
public interface EventAdminController {

    @Operation(summary = "티켓팅 이벤트 생성", description = "새로운 티켓팅 이벤트를 생성합니다. 관리자/매니저 권한이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "티켓팅 이벤트 생성 성공")
    })
    ResponseEntity<SingleResponse<EventResponse>> createEvent(
            @RequestPart("eventContent") @Valid EventCreateRequest eventCreateRequest,
            @RequestPart(value = "eventImage", required = false) @Parameter(description = "이벤트 이미지 파일", content = @Content(mediaType = MediaType.IMAGE_JPEG_VALUE)) MultipartFile eventImage);


    @Operation(summary = "티켓팅 리스트 조회 (관리자)", description = "관리자 페이지에서 티켓팅 이벤트 리스트를 조회합니다. 관리자/매니저 권한이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "[티켓팅 관리자] 이벤트 게시물 리스트 반환 성공")
    })
    ResponseEntity<SingleResponse<EventPageResponse>> getEventListByManager(
            @Parameter(description = "이벤트 상태 (예: all, upcoming, open, ended)", example = "open", required = true) @RequestParam String status,
            @Parameter(description = "페이지 번호", example = "1", required = true) @RequestParam("page") @NotNull int pageNumber);


    @Operation(summary = "티켓팅 이벤트 수정", description = "기존 티켓팅 이벤트를 수정합니다. 관리자/매니저 권한이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "티켓팅 이벤트 수정 성공")
    })
    ResponseEntity<SingleResponse<EventResponse>> updateEvent(
            @Parameter(description = "수정할 이벤트 ID", example = "1", required = true) @PathVariable Long eventId,
            @RequestPart("eventUpdateRequest") @Valid EventUpdateRequest eventUpdateRequest,
            @RequestPart(value = "eventImage", required = false) @Parameter(description = "새로운 이벤트 이미지 파일 (선택 사항)", content = @Content(mediaType = MediaType.IMAGE_JPEG_VALUE)) MultipartFile eventImage);


    @Operation(summary = "티켓팅 이벤트 삭제", description = "지정된 티켓팅 이벤트를 삭제합니다. 관리자/매니저 권한이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "티켓팅 이벤트 삭제 성공")
    })
    ResponseEntity<SingleResponse<Boolean>> deleteEvent(
            @Parameter(description = "삭제할 이벤트 ID", example = "1", required = true) @PathVariable Long eventId);


    @Operation(summary = "이벤트 비밀번호 조회", description = "지정된 이벤트의 비밀번호를 조회합니다. 관리자/매니저 권한이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "티켓팅 이벤트 비밀번호 반환 성공")
    })
    ResponseEntity<SingleResponse<String>> getEventPassword(
            @Parameter(description = "이벤트 ID", example = "1", required = true) @PathVariable Long eventId);


    @Operation(summary = "티켓팅 이벤트 마감", description = "지정된 티켓팅 이벤트를 강제로 마감합니다. 관리자/매니저 권한이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "티켓팅 이벤트 마감 성공")
    })
    ResponseEntity<SingleResponse<Boolean>> closeEvent(
            @Parameter(description = "마감할 이벤트 ID", example = "1", required = true) @PathVariable Long eventId);


    @Operation(summary = "이벤트 수령자 리스트 조회", description = "지정된 이벤트의 참여자(수령자) 리스트를 페이지별로 조회합니다. 관리자/매니저 권한이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이벤트 참여 인원 반환 성공")
    })
    ResponseEntity<SingleResponse<EventParticipationProfilePageResponse>> getEventPart(
            @Parameter(description = "이벤트 ID", example = "1", required = true) @PathVariable Long eventId,
            @Parameter(description = "페이지 번호", example = "1", required = true) @RequestParam("page") @NotNull int pageNumber);


    @Operation(summary = "사용자 티켓 수령 상태 변경", description = "관리자가 특정 사용자의 티켓 수령 상태를 변경합니다 (예: 수령 완료). 관리자/매니저 권한이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수령완료 변경 성공")
    })
    ResponseEntity<SingleResponse<Boolean>> changeReceiveStatus(
            @Parameter(description = "이벤트 ID", example = "1", required = true) @PathVariable Long eventId,
            @Parameter(description = "수령 상태를 변경할 사용자 ID", example = "user123", required = true) @PathVariable String userId,
            @RequestPart(value = "eventImage", required = false) @Parameter(description = "서명 이미지", required = true, content = @Content(mediaType = MediaType.IMAGE_JPEG_VALUE)) MultipartFile eventImage);


    @Operation(summary = "이벤트 잔여 수량 조회", description = "지정된 이벤트의 티켓/상품 잔여 수량을 조회합니다. 관리자/매니저 권한이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "티켓 및 상품 잔여 수량 반환 성공")
    })
    ResponseEntity<SingleResponse<EventStockResponse>> getStock(
            @Parameter(description = "이벤트 ID", example = "1", required = true) @PathVariable Long eventId);


    @Operation(summary = "사용자 티켓팅 취소 (관리자)", description = "관리자가 특정 사용자의 이벤트 티켓팅을 취소합니다. 관리자/매니저 권한이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 티켓 취소 성공")
    })
    ResponseEntity<SingleResponse<?>> cancelTicket(
            @Parameter(description = "이벤트 ID", example = "1", required = true) @PathVariable Long eventId,
            @Parameter(description = "티켓팅을 취소할 사용자 ID", example = "1", required = true) @PathVariable String userId);


    @Operation(summary = "이벤트 수동 오픈", description = "관리자가 지정된 이벤트를 수동으로 오픈합니다. 관리자/매니저 권한이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이벤트 수동 오픈 성공")
    })
    ResponseEntity<SingleResponse<Boolean>> openEvent(
            @Parameter(description = "오픈할 이벤트 ID", example = "1", required = true) @PathVariable Long eventId);
}
