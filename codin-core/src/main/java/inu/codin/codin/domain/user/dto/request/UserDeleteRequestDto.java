package inu.codin.codin.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UserDeleteRequestDto {
    @Schema(description = "이메일 주소", example = "codin@inu.ac.kr")
    @NotBlank
    private String email;
}
