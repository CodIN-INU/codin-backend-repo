package inu.codin.codin.domain.user.dto.request;

import inu.codin.codin.common.dto.Department;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "유저 티켓팅 수령자 정보 Update Dto")
public class UserTicketingParticipationInfoUpdateRequest {

    private Department department;
    private String studentId;

}
