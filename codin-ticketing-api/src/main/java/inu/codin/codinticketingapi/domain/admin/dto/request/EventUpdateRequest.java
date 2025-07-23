package inu.codin.codinticketingapi.domain.admin.dto.request;

import inu.codin.codinticketingapi.domain.ticketing.entity.Campus;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingErrorCode;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Schema(description = "이벤트 수정 요청 DTO")
public class EventUpdateRequest {

    @NotNull(message = "캠퍼스는 필수입니다")
    @Schema(description = "이벤트가 진행되는 캠퍼스", example = "송도 캠퍼스")
    private Campus campus;

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 100, message = "제목은 100자 이하여야 합니다")
    @Schema(description = "이벤트 제목", example = "새내기 환영회 (수정)")
    private String title;

    @Size(max = 1000, message = "설명은 1000자 이하여야 합니다")
    @Schema(description = "이벤트 상세 설명", example = "새내기들을 위한 특별한 환영회입니다. (수정)")
    private String description;

    @Size(max = 200, message = "위치 정보는 200자 이하여야 합니다")
    @Schema(description = "이벤트 장소 정보", example = "학생회관 302호 (수정)")
    private String locationInfo;

    @Size(max = 100, message = "대상자 정보는 100자 이하여야 합니다")
    @Schema(description = "이벤트 대상", example = "전체 재학생 및 졸업생")
    private String target;

    @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
    @Schema(description = "이벤트 재고 수량", example = "120")
    private int quantity;

    @NotNull(message = "이벤트 시작 시간은 필수입니다")
    @Future(message = "이벤트 시작 시간은 현재 시간 이후여야 합니다")
    @Schema(description = "이벤트 시작 시간", example = "2025-07-26T10:00:00")
    private LocalDateTime eventTime;

    @NotNull(message = "이벤트 마감 시간은 필수입니다")
    @Future(message = "이벤트 마감 시간은 현재 시간 이후여야 합니다")
    @Schema(description = "이벤트 마감 시간", example = "2025-07-26T12:00:00")
    private LocalDateTime eventEndTime;

    @Pattern(regexp = "\\d{2,3}-\\d{3,4}-\\d{4}", message = "올바른 전화번호 형식이 아닙니다")
    @Schema(description = "문의 전화번호", example = "02-9876-5432")
    private String inquiryNumber;

    @Size(max = 500, message = "홍보글 링크는 500자 이하여야 합니다")
    @Schema(description = "홍보 링크", example = "https://example.com/promotion/update")
    private String promotionLink;

    /** 시작시간 < 종료시간 검증 */
    public void validateEventTimes() {
        if (eventTime != null && eventEndTime != null && !eventEndTime.isAfter(eventTime)) {
            throw new TicketingException(TicketingErrorCode.ILLEGAL_ARGUMENT);
        }
    }
}
