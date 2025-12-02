package inu.codin.codin.domain.user.dto.request;

import inu.codin.codin.common.dto.Department;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "유저 티켓팅 수령자 정보 Update Dto")
public class UserTicketingParticipationInfoUpdateRequest {

    @Schema(description = "학과", example = "컴퓨터공학부")
    private Department department;

    @Schema(description = "학번", example = "2025123456")
    private String studentId;

    @Schema(description = "이름", example = "횃불이")
    @NotBlank(message = "이름은 비어 있을 수 없습니다.")
    @Size(min = 2, max = 10, message = "이름은 2~10자여야 합니다.")
    private String name;

}
