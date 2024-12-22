package inu.codin.codin.domain.post.domain.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CommentCreateRequestDTO {

    @Schema(description = "댓글 내용", example = "content")
    @NotBlank
    private String content;
}
