package inu.codin.codinticketingapi.domain.ticketing.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import inu.codin.codinticketingapi.domain.admin.entity.Event;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Schema(description = "티켓팅 이벤트 응답 DTO")
public class EventResponse {

    @Schema(description = "티켓팅 이벤트 ID", example = "111111")
    @NotBlank
    private Long eventId;

    @Schema(description = "이벤트 제목", example = "컴퓨터 공학부 중간고사 간식나눔")
    @NotBlank
    private String eventTitle;

    @Schema(description = "이벤트 이미지", example = "https://codin-s3-bucket.s3.ap-northeast-2.amazonaws.com/5eec3638-fda1-40aa-b940-e9124c45bf1c.jpeg")
    private String eventImageUrl;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    @Schema(description = "이벤트 진행 시간", example = "2025-07-02 16:00:00")
    private LocalDateTime eventDate;

    @Schema(description = "이벤트 위치 정보", example = "컴퓨터 공학부 과방")
    private String locationInfo;

    @Schema(description = "이벤트 제공 수량", example = "100")
    private int quantity;

    @Schema(description = "이벤트 현재 수량", example = "80")
    private int currentQuantity;

    public static EventResponse of(Event event) {
        return EventResponse.builder()
                .eventId(event.getId())
                .eventTitle(event.getTitle())
                .eventImageUrl(event.getEventImageUrl())
                .eventDate(event.getEventTime())
                .locationInfo(event.getLocationInfo())
                .quantity(event.getStock().getInitialStock())
                .currentQuantity(event.getStock().getStock())
                .build();
    }
}
