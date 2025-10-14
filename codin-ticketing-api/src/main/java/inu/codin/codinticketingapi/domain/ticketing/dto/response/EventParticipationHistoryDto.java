package inu.codin.codinticketingapi.domain.ticketing.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import inu.codin.codinticketingapi.domain.ticketing.entity.ParticipationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "티켓팅 이벤트 참여 기록 응답 DTO")
public class EventParticipationHistoryDto {

    @Schema(description = "티켓팅 이벤트 ID", example = "111111")
    private Long eventId;

    @Schema(description = "이벤트 제목", example = "중간고사 간식나눔")
    private String title;

    @Schema(description = "이벤트 이미지 URL", example = "https://codin-s3-bucket…/image.jpeg")
    private String eventImageUrl;

    @Schema(description = "이벤트 위치 정보", example = "정보기술대학 514호")
    private String locationInfo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd (E) HH:mm", timezone = "Asia/Seoul")
    @Schema(description = "이벤트 티켓팅 시작 시간", example = "2025.07.02 (수) 16:00")
    private LocalDateTime eventTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd (E) HH:mm", timezone = "Asia/Seoul")
    @Schema(description = "이벤트 티켓팅 종료 시간", example = "2025.07.02 (수) 16:00")
    private LocalDateTime eventEndTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd (E) HH:mm", timezone = "Asia/Seoul")
    @Schema(description = "티켓팅 상품 수령 시작 시간", example = "2025.07.02 (수) 16:00")
    private LocalDateTime eventReceivedStartTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd (E) HH:mm", timezone = "Asia/Seoul")
    @Schema(description = "티켓팅 상품 수령 종료 시간", example = "2025.07.02 (수) 16:00")
    private LocalDateTime eventReceivedEndTime;

    @Schema(description = "참여 상태")
    private ParticipationStatus status;
}
