package inu.codin.codin.domain.email.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 이메일 인증 코드 전송 요청 DTO
 * 해당 이메일로 인증 코드를 전송한다.
 */
@Data
public class JoinEmailSendRequestDto {

    @Schema(description = "이메일 주소", example = "example@inu.ac.kr")
    @Email @NotNull @NotEmpty
    private String email;

}
