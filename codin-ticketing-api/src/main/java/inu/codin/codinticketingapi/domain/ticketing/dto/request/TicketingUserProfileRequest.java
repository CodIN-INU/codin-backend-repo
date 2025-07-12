package inu.codin.codinticketingapi.domain.ticketing.dto.request;

import inu.codin.codinticketingapi.domain.ticketing.entity.Department;
import inu.codin.codinticketingapi.domain.ticketing.entity.Profile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class TicketingUserProfileRequest {

    @Schema(description = "학과 정보 (COMPUTER_SCI, INFO_COMM, EMBEDDED)", example = "COMPUTER_SCI")
    private Department department;

    @Schema(description = "학번", example = "202501111")
    private String studentId;

    public static Profile toEntity(TicketingUserProfileRequest requestDto, String userId, String username) {
        return Profile.builder()
                .userId(userId)
                .name(username)
                .department(requestDto.getDepartment())
                .studentId(requestDto.getStudentId())
                .build();
    }
}
