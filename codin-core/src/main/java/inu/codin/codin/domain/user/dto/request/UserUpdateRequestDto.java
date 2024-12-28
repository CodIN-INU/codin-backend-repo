package inu.codin.codin.domain.user.dto.request;

import inu.codin.codin.common.Department;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UserUpdateRequestDto {
    @Schema(description = "학번", example = "20210000")
    @NotBlank
    private String studentId;

    @Schema(description = "이름", example = "홍길동")
    @NotBlank
    private String name;

    @Schema(description = "닉네임", example = "코딩")
    @NotBlank
    private String nickname;

    @Schema(description = "프로필 이미지 URL", example = "https://avatars.githubusercontent.com/u/77490521?v=4")
    @NotBlank
    private String profileImageUrl;

    @Schema(description = "소속", example = "IT_COLLEGE")
    private Department department;
}
