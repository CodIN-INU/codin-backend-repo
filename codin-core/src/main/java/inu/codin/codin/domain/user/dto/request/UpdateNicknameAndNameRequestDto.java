package inu.codin.codin.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateNicknameAndNameRequestDto(

        @Schema(description = "닉네임", example = "코딩")
        String nickname,

        @Schema(description = "이름", example = "김철수")
        String name
) {
}
