package inu.codin.codinticketingapi.domain.ticketing.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import inu.codin.codinticketingapi.domain.ticketing.entity.TicketingEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class EventResponse {
    @Schema(description = "티켓팅 이벤트 ID", example = "111111")
    @NotBlank
    private String eventId;

    @Schema(description = "이벤트 제목", example = "컴퓨터 공학부 중간고사 간식나눔")
    @NotBlank
    private String eventTitle;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    @Schema(description = "이벤트 진행 시간", example = "2025-07-02 16:00:00")
    private LocalDateTime eventDate;

    @Schema(description = "이벤트 위치 정보", example = "컴퓨터 공학부 과방")
    private String locationInfo;

    @Schema(description = "이벤트 수량", example = "100")
    private int quantity;

    public static EventResponse of(TicketingEvent event) {
        return EventResponse.builder()
                .eventId(event.get_id().toHexString())
                .eventTitle(event.getTitle())
                .eventDate(event.getEventTime())
                .locationInfo(event.getLocationInfo())
                .quantity(event.getQuantity())
                .build();
    }
}
