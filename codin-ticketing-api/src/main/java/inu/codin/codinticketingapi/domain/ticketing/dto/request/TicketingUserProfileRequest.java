package inu.codin.codinticketingapi.domain.ticketing.dto.request;

import inu.codin.codinticketingapi.domain.ticketing.entity.Department;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class TicketingUserProfileRequest {

    @Schema(description = "학과 정보")
    private Department department;

    @Schema(description = "학번")
    private String studentId;
}
