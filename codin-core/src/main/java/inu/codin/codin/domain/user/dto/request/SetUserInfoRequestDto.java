package inu.codin.codin.domain.user.dto.request;

import inu.codin.common.entity.College;
import inu.codin.common.entity.Department;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SetUserInfoRequestDto(

        @Schema(description = "닉네임", example = "코딩")
        @NotBlank
        String nickname,

        @Schema(description = "이름", example = "김철수")
        @NotBlank
        String name,

        @Schema(description = "단과대", example = "정보기술대학")
        @NotNull
        College college,

        @Schema(description = "학과", example = "컴퓨터공학부")
        @NotNull
        Department department
) {
}
