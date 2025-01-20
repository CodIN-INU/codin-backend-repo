package inu.codin.codin.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSignUpRequestDto {

    @NotBlank
    private String studentId;

    @NotBlank
    private String password;
}
