package inu.codin.codin.common.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpAndLoginRequestDto {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
}
