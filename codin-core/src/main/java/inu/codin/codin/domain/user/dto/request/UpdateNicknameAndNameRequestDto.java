package inu.codin.codin.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UpdateNicknameAndNameRequestDto(

        @Schema(description = "닉네임", example = "코딩")
        @NotBlank
        String nickname,

        @Schema(description = "이름", example = "김철수")
        @NotBlank
        String name
) {
}
