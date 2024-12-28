package inu.codin.codin.domain.user.dto.request;

import inu.codin.codin.common.Department;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UserCreateRequestDto {

    // todo : 길이 관련 validation 추가

    @Schema(description = "이메일 주소", example = "codin@inu.ac.kr")
    @NotBlank
    private String email;

    @Schema(description = "비밀번호", example = "password")
    @NotBlank
    private String password;

    @Schema(description = "학번", example = "20210000")
    @NotBlank
    private String studentId;

    @Schema(description = "이름", example = "홍길동")
    @NotBlank
    private String name;

    @Schema(description = "닉네임", example = "코딩")
    @NotBlank
    private String nickname;

    @Schema(description = "소속", example = "IT_COLLEGE")
    private Department department;

}