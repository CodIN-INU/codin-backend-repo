package inu.codin.codinticketingapi.domain.ticketing.dto.response;

import inu.codin.codinticketingapi.domain.ticketing.entity.Participation;
import inu.codin.codinticketingapi.domain.ticketing.entity.ParticipationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "티켓팅 이벤트 수령 생성 응답 DTO")
public class ParticipationResponse {

    @Schema(description = "참여 상태", example = "PENDING")
    private ParticipationStatus status;

    @Schema(description = "이벤트 티켓 번호", example = "1")
    private Integer ticketNumber;

    @Schema(description = "서명 이미지 URL", example = "https://codin-s3-bucket.s3.ap-northeast-2.amazonaws.com/signature.jpeg")
    private String signatureImgUrl;

    public static ParticipationResponse of(Participation participation) {
        return ParticipationResponse.builder()
                .status(participation.getStatus())
                .ticketNumber(participation.getTicketNumber())
                .signatureImgUrl(participation.getSignatureImgUrl())
                .build();
    }
}
