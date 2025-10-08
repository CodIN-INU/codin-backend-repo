package inu.codin.codinticketingapi.domain.ticketing.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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
@Schema(description = "이벤트 세부 정보 응답 DTO")
public class EventDetailResponse {
    @Schema(description = "티켓팅 이벤트 ID", example = "111111")
    private Long eventId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    @Schema(description = "이벤트 진행 시간", example = "2025-07-02 16:00")
    private LocalDateTime eventTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    @Schema(description = "이벤트 마감 시간", example = "2025-07-02 16:00")
    private LocalDateTime eventEndTime;

    @Schema(description = "이벤트 이미지 Url")
    private String eventImageUrls;

    @Schema(description = "이벤트 제목", example = "중간고사 간식나눔")
    private String eventTitle;

    @Schema(description = "이벤트 위치 정보", example = "정보기술대학 514호 ..")
    private String locationInfo;

    @Schema(description = "이벤트 제공 수량", example = "100")
    private int quantity;

    @Schema(description = "이벤트 현재 수량", example = "80")
    private int currentQuantity;

    @Schema(description = "이벤트 대상", example = "컴퓨터 공학부 재학생")
    private String target;

    @Schema(description = "이벤트 부가 설명", example = "이벤트 부가 설명문 ~~")
    private String description;

    @Schema(description = "이벤트 담당자 문의 번호", example = "010-1234-5678")
    private String inquiryNumber;

    @Schema(description = "홍보글 링크", example = "www.example.com")
    private String promotionLink;

    @Schema(description = "이벤트 상태", example = "UPCOMING, ACTIVE, ENDED")
    private EventStatus eventStatus;

    @Schema(description = "유저 티켓팅 정보 존재 여부", example = "true, false")
    private boolean isExistParticipationData;

    public static EventDetailResponse of(Event event,  boolean isExistParticipationData) {
        return EventDetailResponse.builder()
                .eventId(event.getId())
                .eventTime(event.getEventTime())
                .eventEndTime(event.getEventEndTime())
                .eventImageUrls(event.getEventImageUrl())
                .eventTitle(event.getTitle())
                .locationInfo(event.getLocationInfo())
                .quantity(event.getStock().getCurrentTotalStock())
                .currentQuantity(event.getStock().getRemainingStock())
                .target(event.getTarget())
                .description(event.getDescription())
                .inquiryNumber(event.getInquiryNumber())
                .promotionLink(event.getPromotionLink())
                .eventStatus(event.getEventStatus())
                .isExistParticipationData(isExistParticipationData)
                .build();
    }
}
