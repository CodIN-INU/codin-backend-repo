package inu.codin.codin.domain.calendar.controller.swagger;

import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.calendar.dto.CalendarCreateRequest;
import inu.codin.codin.domain.calendar.dto.CalendarCreateResponse;
import inu.codin.codin.domain.calendar.dto.CalendarMonthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Calendar API", description = "[리디자인] 캘린더 API")
public interface CalendarController {

    @Operation(summary = "월별 캘린더 조회", description = "특정 년도와 월의 캘린더 이벤트를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "캘린더 조회 성공"),
    })
    ResponseEntity<SingleResponse<CalendarMonthResponse>> getMonth(
            @Parameter(description = "년도", example = "2025") @RequestParam int year,
            @Parameter(description = "월", example = "8") @RequestParam int month
    );

    @Operation(summary = "캘린더 이벤트 생성", description = "새로운 캘린더 이벤트를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "이벤트 생성 성공"),
    })
    ResponseEntity<SingleResponse<CalendarCreateResponse>> create(
            @Valid @RequestBody CalendarCreateRequest request
    );

    @Operation(summary = "캘린더 이벤트 삭제", description = "특정 캘린더 이벤트를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "이벤트 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "이벤트를 찾을 수 없음")
    })
    ResponseEntity<SingleResponse<?>> delete(
            @Parameter(description = "이벤트 ID", example = "68ab0ac4ffbcdc7080e8d663") @PathVariable String eventId
    );
}
