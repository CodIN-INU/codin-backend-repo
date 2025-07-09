package inu.codin.codinticketingapi.domain.ticketing.dto.response;

import inu.codin.codinticketingapi.domain.ticketing.entity.TicketingProfile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserTicketingProfileResponse {

    @Schema(description = "소속 부서 (컴퓨터공학부, 정보통신공학과, 임베디드시스템공학과)", example = "컴퓨터공학부")
    private String department;

    @Schema(description = "학번", example = "202501111")
    private String studentId;

    public static UserTicketingProfileResponse of(TicketingProfile ticketingProfile) {
        return UserTicketingProfileResponse.builder()
                .department(ticketingProfile.getDepartment().toValue())
                .studentId(ticketingProfile.getStudentId())
                .build();
    }
}