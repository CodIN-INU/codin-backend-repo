package inu.codin.codin.common.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpAndLoginRequestDto {

    @NotBlank
    @Size(min = 9 , max = 9 , message = "학번은 9자리여야 합니다.")
    private String studentId;

    @NotBlank
    private String password;
}
