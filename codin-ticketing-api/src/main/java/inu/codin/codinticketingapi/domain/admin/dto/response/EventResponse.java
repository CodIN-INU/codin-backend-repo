package inu.codin.codinticketingapi.domain.admin.dto.response;

import inu.codin.codinticketingapi.domain.admin.entity.Event;
import inu.codin.codinticketingapi.domain.admin.entity.EventStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Schema(description = "이벤트 상세 정보를 담는 응답 DTO")
public class EventResponse {
    @Schema(description = "이벤트 ID", example = "1")
    private Long id;
    @Schema(description = "이벤트가 진행되는 캠퍼스", example = "송도 캠퍼스")
    private String campus;
    @Schema(description = "이벤트 시작 시간", example = "2025-07-25T10:00:00")
    private LocalDateTime eventTime;
    @Schema(description = "이벤트 종료 시간", example = "2025-07-25T12:00:00")
    private LocalDateTime eventEndTime;
    @Schema(description = "이벤트 이미지 URL", example = "https://example.com/image.jpg")
    private String eventImageUrl;
    @Schema(description = "이벤트 제목", example = "새내기 환영회")
    private String title;
    @Schema(description = "이벤트 장소 정보", example = "학생회관 301호")
    private String locationInfo;
    @Schema(description = "이벤트 재고 수량", example = "100")
    private int stock;
    @Schema(description = "이벤트 대상", example = "전체 재학생")
    private String target;
    @Schema(description = "이벤트 상세 설명", example = "새내기들을 위한 특별한 환영회입니다.")
    private String description;
    @Schema(description = "문의 전화번호", example = "02-1234-5678")
    private String inquiryNumber;
    @Schema(description = "홍보 링크", example = "https://example.com/promotion")
    private String promotionLink;
    @Schema(description = "이벤트 상태", example = "UPCOMING")
    private EventStatus status;

    public static EventResponse of(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .campus(event.getCampus().getDescription())
                .eventTime(event.getEventTime())
                .eventEndTime(event.getEventEndTime())
                .eventImageUrl(event.getEventImageUrl())
                .title(event.getTitle())
                .locationInfo(event.getLocationInfo())
                .stock(event.getStock().getStock())
                .target(event.getTarget())
                .description(event.getDescription())
                .inquiryNumber(event.getInquiryNumber())
                .promotionLink(event.getPromotionLink())
                .status(event.getEventStatus())
                .build();
    }
}
