package inu.codin.codin.domain.post.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PostContentUpdateReqDTO {

    @Schema(description = "게시물 제목", example = "Updated Title")
    @NotBlank
    private String title;

    @Schema(description = "게시물 내용", example = "Updated content")
    @NotBlank
    private String content;

    @Schema(description = "게시물 내 이미지 url", example = "example/updated_image.jpg")
    private String postImageUrl;
}