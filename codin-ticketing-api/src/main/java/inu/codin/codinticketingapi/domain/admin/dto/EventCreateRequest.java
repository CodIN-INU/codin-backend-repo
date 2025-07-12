package inu.codin.codinticketingapi.domain.admin.dto;

import inu.codin.codinticketingapi.domain.ticketing.entity.Campus;
import inu.codin.codinticketingapi.domain.admin.entity.Event;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingErrorCode;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingException;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class EventCreateRequest {

    @NotNull(message = "캠퍼스는 필수입니다")
    private Campus campus;

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 100, message = "제목은 100자 이하여야 합니다")
    private String title;

    @Size(max = 1000, message = "설명은 1000자 이하여야 합니다")
    private String description;

    @Size(max = 200, message = "위치 정보는 200자 이하여야 합니다")
    private String locationInfo;

    @Size(max = 100, message = "대상자 정보는 100자 이하여야 합니다")
    private String target;

    @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
    private int quantity;

    @NotNull(message = "이벤트 시작 시간은 필수입니다")
    @Future(message = "이벤트 시작 시간은 현재 시간보다 나중이어야 합니다")
    private LocalDateTime eventTime;

    @NotNull(message = "이벤트 마감 시간은 필수입니다")
    @Future(message = "이벤트 마감 시간은 현재 시간보다 나중이어야 합니다")
    private LocalDateTime eventEndTime;

    @Pattern(regexp = "\\d{2,3}-\\d{3,4}-\\d{4}", message = "올바른 전화번호 형식이 아닙니다")
    private String inquiryNumber;

    @Size(max = 500, message = "홍보글 링크는 500자 이하여야 합니다")
    private String promotionLink;

    public Event toEntity(String userId, String eventImageUrl) {
        return Event.builder()
                .userId(userId)
                .campus(this.campus)
                .eventTime(this.eventTime)
                .eventEndTime(this.eventEndTime)
                .eventImageUrl(eventImageUrl)
                .title(this.title)
                .locationInfo(this.locationInfo)
                .quantity(this.quantity)
                .target(this.target)
                .description(this.description)
                .inquiryNumber(this.inquiryNumber)
                .promotionLink(this.promotionLink)
                .build();
    }

    /** 이벤트 시간 검증
     *  - 이벤트 종료 시간은 시작 시간 이후
     * */
    public void validateEventTimes() {
        if (eventEndTime != null && eventTime != null && !eventEndTime.isAfter(eventTime)) {
            throw new TicketingException(TicketingErrorCode.ILLEGAL_ARGUMENT);
        }
    }
}