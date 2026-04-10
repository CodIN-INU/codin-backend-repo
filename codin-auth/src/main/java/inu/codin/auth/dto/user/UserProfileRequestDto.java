package inu.codin.auth.dto.user;

import inu.codin.common.entity.College;
import inu.codin.common.entity.Department;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UserProfileRequestDto {

    @Schema(description = "이메일", example = "1234@inu.ac.kr")
    @Email
    @NotBlank
    private String email;

    @Schema(description = "닉네임", example = "코딩")
    @NotBlank
    private String nickname;

    @Schema(description = "이름", example = "김철수")
    @NotBlank
    private String name;

    @Schema(description = "단과대", example = "INFORMATION_TECHNOLOGY")
    @NotNull
    private College college;

    @Schema(description = "학과", example = "COMPUTER_SCI")
    @NotNull
    private Department department;

    @Schema(description = "학번", example = "20201234")
    @NotBlank
    private String studentId;
}
