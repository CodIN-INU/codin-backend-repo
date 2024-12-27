package inu.codin.codin.domain.post.domain.reply.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ReplyAnonnymousUpdateRequestDTO {
    @Schema(description = "익명 여부", example = "true")
    @NotNull
    private boolean anonymous;
}
