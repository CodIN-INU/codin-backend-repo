package inu.codin.codin.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

}
