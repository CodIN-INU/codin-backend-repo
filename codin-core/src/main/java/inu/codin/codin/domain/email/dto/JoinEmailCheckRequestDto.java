package inu.codin.codin.domain.email.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

/**
 * 이메일 인증 코드 확인 요청 DTO
 * 해당 이메일로 전송된 인증 코드를 확인한다.
 */
@Getter
@Builder
public class JoinEmailCheckRequestDto {

    @Schema(description = "이메일 주소", example = "example@inu.ac.kr")
    @Email @NotBlank
    private String email;

    @Schema(description = "인증 코드 (6자리)", example = "123456")
    @NotBlank
    private String authNum;
}
