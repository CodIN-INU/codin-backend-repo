package inu.codin.codin.domain.post.dto.request;

import inu.codin.codin.domain.post.entity.PostCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PostCreateRequestDTO {

    @Schema(description = "게시물 제목", example = "Example")
    @NotBlank
    private String title;

    @Schema(description = "게시물 내용", example = "example content")
    @NotBlank
    private String content;

    //이미지 별도 Multipart (RequestPart 사용)

    @Schema(description = "게시물 익명 여부 default = 0 (익명)", example = "true")
    @NotNull
    private boolean isAnonymous;

    @Schema(description = "게시물 종류", example = "REQUEST_STUDY")
    @NotNull
    private PostCategory postCategory;
    //STATUS 필드 - DEFAULT :: ACTIVE

}