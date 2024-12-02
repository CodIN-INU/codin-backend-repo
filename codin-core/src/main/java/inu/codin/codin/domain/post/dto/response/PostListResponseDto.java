package inu.codin.codin.domain.post.dto.response;

import inu.codin.codin.domain.post.entity.PostCategory;
import inu.codin.codin.domain.post.entity.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PostListResponseDto extends PostCommonResponseDto{

    @Schema(description = "댓글 및 대댓글 count", example = "0")
    private int commentCount;

    public PostListResponseDto(String userId, String postId, String content, String title, PostCategory postCategory, PostStatus postStatus, List<String> postImageUrls , boolean isAnonymous, int commentCount, int likeCount, int scrapCount) {
        super(userId, postId, content, title, postCategory, postImageUrls, isAnonymous, likeCount, scrapCount);
        this.commentCount = commentCount;
    }
}