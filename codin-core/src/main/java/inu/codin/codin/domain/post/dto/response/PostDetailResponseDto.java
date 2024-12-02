package inu.codin.codin.domain.post.dto.response;

import inu.codin.codin.domain.post.domain.comment.dto.CommentResponseDTO;
import inu.codin.codin.domain.post.entity.PostCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PostDetailResponseDto extends PostCommonResponseDto{
    @Schema(description = "댓글 및 대댓글 데이터")
    private List<CommentResponseDTO> comments;

    // Constructor for partial initialization
    public PostDetailResponseDto(
            String userId,
            String postId,
            String content,
            String title,
            PostCategory postCategory,
            List<String> postImageUrls,
            boolean isAnonymous,
            List<CommentResponseDTO> comments,
            int likeCount,
            int scrapCount
    ) {
        super(userId, postId, content, title, postCategory, postImageUrls, isAnonymous, likeCount, scrapCount);
        this.comments = comments;
    }
}