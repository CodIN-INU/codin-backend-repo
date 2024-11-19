package inu.codin.codin.common.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDto {

    @Schema(description = "이메일 주소", example = "codin@gmail.com")
    @NotBlank
    private String email;

    @Schema(description = "비밀번호", example = "1234")
    @NotBlank
    private String password;

}
