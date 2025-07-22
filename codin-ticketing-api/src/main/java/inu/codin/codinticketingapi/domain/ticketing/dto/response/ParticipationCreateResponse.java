package inu.codin.codinticketingapi.domain.ticketing.dto.response;

import inu.codin.codinticketingapi.domain.ticketing.entity.Participation;
import inu.codin.codinticketingapi.domain.ticketing.entity.ParticipationStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParticipationCreateResponse {

    private ParticipationStatus status;
    private Integer ticketNumber;
    private String signatureImgUrl;

    public static ParticipationCreateResponse of(Participation participation) {
        return ParticipationCreateResponse.builder()
                .status(participation.getStatus())
                .ticketNumber(participation.getTicketNumber())
                .signatureImgUrl(participation.getSignatureImgUrl())
                .build();
    }
}
