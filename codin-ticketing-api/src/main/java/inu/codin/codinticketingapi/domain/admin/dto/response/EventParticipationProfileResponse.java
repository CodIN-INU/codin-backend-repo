package inu.codin.codinticketingapi.domain.admin.dto.response;

import inu.codin.codinticketingapi.domain.ticketing.entity.Department;
import inu.codin.codinticketingapi.domain.ticketing.entity.Participation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EventParticipationProfileResponse {
    @Schema(description = "사용자 ID", example = "1")
    private String userId;
    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;
    @Schema(description = "학번", example = "202012345")
    private String studentId;
    @Schema(description = "학과", example = "COMPUTER_SCIENCE")
    private Department department;
    @Schema(description = "서명 이미지 URL", example = "https://example.com/signature.png")
    private String imageURL;

    public static EventParticipationProfileResponse of(Participation participation) {
        return EventParticipationProfileResponse.builder()
                .userId(participation.getUserId())
                .name(participation.getName())
                .studentId(participation.getStudentId())
                .department(participation.getDepartment())
                .imageURL(participation.getSignatureImgUrl())
                .build();
    }
}
