package inu.codin.codin.domain.post.dto.request;

import inu.codin.codin.domain.post.entity.PostCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PostCreateReqDTO {

    @Schema(description = "유저 ID", example = "111111")
    @NotBlank
    private String userId;

    @Schema(description = "게시물 종류", example = "구해요")
    @NotNull
    private PostCategory postCategory;

    @Schema(description = "게시물 제목", example = "Example")
    @NotBlank
    private String title;

    @Schema(description = "게시물 내용", example = "example content")
    @NotBlank
    private String content;

    @Schema(description = "게시물 내 이미지 url , blank 가능", example = "example/1231")
    private String postImageUrl;

    @Schema(description = "게시물 익명 여부 default = true (익명)", example = "true")
    @NotNull
    private boolean isAnonymous;

    //STATUS 필드 - DEFAULT :: ACTIVE

}
