package inu.codin.codin.domain.post.dto.response;

import inu.codin.codin.domain.post.entity.PostCategory;
import inu.codin.codin.domain.post.entity.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
public class PostWithDetailResponseDTO {

    @Schema(description = "게시물 작성자 ID", example = "user123")
    @NotBlank
    private String userId;

    @Schema(description = "게시물 ID", example = "post123")
    @NotBlank
    private String postId;

    @Schema(description = "게시물 내용", example = "This is a detailed content of the post.")
    @NotBlank
    private String content;

    @Schema(description = "게시물 제목", example = "Post Title")
    @NotBlank
    private String title;

    @Schema(description = "게시물 카테고리", example = "구해요")
    @NotNull
    private PostCategory postCategory;

    @Schema(description = "게시물 상태", example = "ACTIVE")
    @NotNull
    private PostStatus postStatus;

    @Schema(description = "게시물 이미지 URL 리스트", example = "[\"image1.jpg\", \"image2.jpg\"]")
    private List<String> postImageUrls;

    @Schema(description = "게시물 익명 여부", example = "true")
    private boolean isAnonymous;

    @Schema(description = "댓글 및 대댓글 데이터")
    private List<CommentsResponseDTO> comments;

    @Schema(description = "좋아요 수", example = "10")
    private int likeCount;

    @Schema(description = "스크랩 수", example = "5")
    private int scrapCount;

    // Constructor for partial initialization
    public PostWithDetailResponseDTO(
            String userId,
            String postId,
            String content,
            String title,
            PostCategory postCategory,
            PostStatus postStatus,
            List<String> postImageUrls,
            boolean isAnonymous,
            List<CommentsResponseDTO> comments,
            int likeCount,
            int scrapCount
    ) {
        this.userId = userId;
        this.postId = postId;
        this.content = content;
        this.title = title;
        this.postCategory = postCategory;
        this.postStatus = postStatus;
        this.postImageUrls = postImageUrls;
        this.isAnonymous = isAnonymous;
        this.comments = comments;
        this.likeCount = likeCount;
        this.scrapCount = scrapCount;
    }
}