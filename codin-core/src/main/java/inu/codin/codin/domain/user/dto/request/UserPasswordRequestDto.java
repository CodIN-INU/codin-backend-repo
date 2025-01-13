package inu.codin.codin.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UserPasswordRequestDto {

    @Schema(description = "변경된 비밀번호", example = "password1234")
    @NotBlank
    private String password;

}
