package inu.codin.codinticketingapi.domain.admin.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;

@Tag(name = "Event 엑셀 API", description = "티켓팅 이벤트 엑셀 정보 API")
public interface ExcelController {

    @Operation(summary = "이벤트 참가자 엑셀 다운로드",
            description = "특정 이벤트의 참가자 목록을 엑셀 파일로 다운로드합니다. 매니저 또는 관리자 권한이 필요합니다.",
            parameters = @Parameter(name = "eventId", description = "엑셀을 다운로드할 이벤트 ID", required = true, example = "1"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "엑셀 파일 다운로드 성공",
                            content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            })
    ResponseEntity<ByteArrayResource> downloadEventExcel(Long eventId);
}