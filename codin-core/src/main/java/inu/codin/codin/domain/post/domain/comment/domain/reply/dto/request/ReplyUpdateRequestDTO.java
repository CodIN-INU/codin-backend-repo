package inu.codin.codin.domain.post.domain.comment.domain.reply.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReplyUpdateRequestDTO {

    @Schema(description = "댓글 내용", example = "content")
    @NotBlank
    private String content;
}

