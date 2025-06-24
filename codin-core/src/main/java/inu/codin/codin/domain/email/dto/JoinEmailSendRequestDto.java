package inu.codin.codin.domain.email.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

/**
 * 이메일 인증 코드 전송 요청 DTO
 * 해당 이메일로 인증 코드를 전송한다.
 */
@Getter
@Builder
public class JoinEmailSendRequestDto {

    @Schema(description = "이메일 주소", example = "example@inu.ac.kr")
    @Email @NotBlank
    private String email;

}
