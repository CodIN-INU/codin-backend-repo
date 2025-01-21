package inu.codin.codin.common.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpAndLoginRequestDto {

    @NotBlank
    private String studentId;

    @NotBlank
    private String password;
}
