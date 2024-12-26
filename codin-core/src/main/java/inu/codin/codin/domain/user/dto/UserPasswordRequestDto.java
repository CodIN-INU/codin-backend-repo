package inu.codin.codin.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UserPasswordRequestDto {

    @Schema(description = "이메일 주소", example = "example@inu.ac.kr")
    @Email
    @NotBlank
    private String email;

    @Schema(description = "변경된 비밀번호", example = "password1234")
    private String password;

}
