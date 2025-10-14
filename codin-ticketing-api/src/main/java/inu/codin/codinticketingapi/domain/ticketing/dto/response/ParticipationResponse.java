package inu.codin.codinticketingapi.domain.ticketing.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import inu.codin.codinticketingapi.domain.ticketing.entity.Participation;
import inu.codin.codinticketingapi.domain.ticketing.entity.ParticipationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "티켓팅 이벤트 수령 생성 응답 DTO")
public class ParticipationResponse {

    @Schema(description = "참여 상태", example = "PENDING")
    private ParticipationStatus status;

    @Schema(description = "이벤트 티켓 번호", example = "1")
    private Integer ticketNumber;

    @Schema(description = "서명 이미지 URL", example = "https://codin-s3-bucket.s3.ap-northeast-2.amazonaws.com/signature.jpeg")
    private String signatureImgUrl;

    @Schema(description = "이벤트 수령 시작 시간", example = "2025-07-25T12:00:00")
    private LocalDateTime eventReceivedStartTime;
    @Schema(description = "이벤트 수령 종료 시간", example = "2025-07-25T12:00:00")
    private LocalDateTime eventReceivedEndTime;

    @Schema(description = "이벤트 장소 정보", example = "학생회관 301호")
    private String locationInfo;

    @JsonCreator
    public ParticipationResponse(
            @JsonProperty("status") ParticipationStatus status,
            @JsonProperty("ticketNumber") Integer ticketNumber,
            @JsonProperty("signatureImgUrl") String signatureImgUrl,
            @JsonProperty("eventReceivedStartTime") LocalDateTime eventReceivedStartTime,
            @JsonProperty("eventReceivedEndTime") LocalDateTime eventReceivedEndTime,
            @JsonProperty("locationInfo") String locationInfo
    ) {
        this.status = status;
        this.ticketNumber = ticketNumber;
        this.signatureImgUrl = signatureImgUrl;
        this.eventReceivedStartTime = eventReceivedStartTime;
        this.eventReceivedEndTime = eventReceivedEndTime;
        this.locationInfo = locationInfo;
    }

    public static ParticipationResponse of(Participation participation) {
        return ParticipationResponse.builder()
                .status(participation.getStatus())
                .ticketNumber(participation.getTicketNumber())
                .signatureImgUrl(participation.getSignatureImgUrl())
                .eventReceivedStartTime(participation.getEvent().getEventReceivedStartTime())
                .eventReceivedEndTime(participation.getEvent().getEventReceivedEndTime())
                .locationInfo(participation.getEvent().getLocationInfo())
                .build();
    }
}
