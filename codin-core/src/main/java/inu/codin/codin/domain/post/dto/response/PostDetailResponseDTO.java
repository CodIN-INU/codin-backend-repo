package inu.codin.codin.domain.post.dto.response;

import inu.codin.codin.domain.post.entity.PostCategory;
import inu.codin.codin.domain.post.entity.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PostDetailResponseDTO {

    @Schema(description = "유저 ID", example = "111111")
    @NotBlank
    private String userId;

    @Schema(description = "게시물 ID", example = "111111")
    @NotBlank
    private String postId;

    @Schema(description = "게시물 종류", example = "구해요")
    @NotBlank
    private PostCategory postCategory;

    @Schema(description = "게시물 제목", example = "Example")
    @NotBlank
    private String title;

    @Schema(description = "게시물 내용", example = "example content")
    @NotBlank
    private String content;

    @Schema(description = "게시물 내 이미지 url , blank 가능", example = "example/1231")
    private List<String> postImageUrl;

    @Schema(description = "게시물 익명 여부 default = 0 (익명)", example = "0")
    @NotNull
    private boolean isAnonymous;

    public PostDetailResponseDTO(String userId, String postId, String content, String title, PostCategory postCategory, PostStatus postStatus, List<String> postImageUrls , boolean isAnonymous) {
        this.userId = userId;
        this.postId = postId;
        this.content = content;
        this.title = title;
        this.postCategory = postCategory;
        this.postImageUrl = postImageUrls;
        this.isAnonymous = isAnonymous;
    }
}
