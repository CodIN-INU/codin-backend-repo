package inu.codin.codin.domain.user.dto.request;

import inu.codin.common.entity.College;
import inu.codin.common.entity.Department;
import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateUserInfoRequestDto(

        @Schema(description = "닉네임", example = "코딩")
        String nickname,

        @Schema(description = "이름", example = "김철수")
        String name,

        @Schema(description = "단과대", example = "INFORMATION_TECHNOLOGY")
        College college,

        @Schema(description = "학과", example = "COMPUTER_SCI")
        Department department,

        @Schema(description = "학번", example = "202012345")
        String studentId
) {
}
