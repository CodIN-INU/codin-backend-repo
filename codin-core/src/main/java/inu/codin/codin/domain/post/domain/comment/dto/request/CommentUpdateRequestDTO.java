package inu.codin.codin.domain.post.domain.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentUpdateRequestDTO {
    @Schema(description = "댓글 내용", example = "content")
    @NotBlank
    private String content;
}
