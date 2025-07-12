package inu.codin.codinticketingapi.domain.ticketing.dto.response;

import inu.codin.codinticketingapi.domain.ticketing.entity.Profile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserTicketingProfileResponse {

    @Schema(description = "학과 정보 (COMPUTER_SCI, INFO_COMM, EMBEDDED)", example = "COMPUTER_SCI")
    private String department;

    @Schema(description = "이름")
    private String name;

    @Schema(description = "학번", example = "202501111")
    private String studentId;

    public static UserTicketingProfileResponse of(Profile profile) {
        return UserTicketingProfileResponse.builder()
                .department(profile.getDepartment().toValue())
                .name(profile.getName())
                .studentId(profile.getStudentId())
                .build();
    }
}