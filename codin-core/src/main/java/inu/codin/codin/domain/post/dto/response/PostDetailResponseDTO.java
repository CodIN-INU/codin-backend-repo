package inu.codin.codin.domain.post.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import inu.codin.codin.domain.post.entity.PostCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PostDetailResponseDTO {
    @Schema(description = "게시물 ID", example = "111111")
    @NotBlank
    private final String _id;

    @Schema(description = "유저 ID", example = "111111")
    @NotBlank
    private final String userId;

    @Schema(description = "게시물 종류", example = "구해요")
    @NotBlank
    private final PostCategory postCategory;

    @Schema(description = "게시물 제목", example = "Example")
    @NotBlank
    private final String title;

    @Schema(description = "게시물 내용", example = "example content")
    @NotBlank
    private final String content;

    @Schema(description = "게시물 내 이미지 url , blank 가능", example = "example/1231")
    private final List<String> postImageUrl;

    @Schema(description = "게시물 익명 여부 default = 0 (익명)", example = "0")
    @NotNull
    private final boolean isAnonymous;

    @Schema(description = "좋아요 count", example = "0")
    private final int likeCount;

    @Schema(description = "스크랩 count", example = "0")
    private final int scrapCount;

    @Schema(description = "댓글 및 대댓글 count", example = "0")
    private final int commentCount;

    @Schema(description = "조회수", example = "0")
    private final int hits;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    @Schema(description = "생성 일자", example = "2024-12-02 20:10:18")
    private final LocalDateTime createdAt;

    @Schema(description = "해당 게시글에 대한 유저 반응 여부")
    private final UserInfo userInfo;

    public PostDetailResponseDTO(String userId, String _id, String content, String title, PostCategory postCategory, List < String > postImageUrls,
                            boolean isAnonymous, int likeCount, int scrapCount, int hits, LocalDateTime createdAt, int commentCount, UserInfo userInfo){
        this.userId = userId;
        this._id = _id;
        this.content = content;
        this.title = title;
        this.postCategory = postCategory;
        this.postImageUrl = postImageUrls;
        this.isAnonymous = isAnonymous;
        this.likeCount = likeCount;
        this.scrapCount = scrapCount;
        this.commentCount = commentCount;
        this.hits = hits;
        this.createdAt = createdAt;
        this.userInfo = userInfo;
    }

    @Getter
    public static class UserInfo {
        private final boolean isLike;
        private final boolean isScrap;

        @Builder
        public UserInfo(boolean isLike, boolean isScrap) {
            this.isLike = isLike;
            this.isScrap = isScrap;
        }
    }
}

