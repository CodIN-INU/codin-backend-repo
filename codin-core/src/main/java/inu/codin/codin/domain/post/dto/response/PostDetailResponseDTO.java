package inu.codin.codin.domain.post.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import inu.codin.codin.domain.post.entity.PostCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @Schema(description = "유저 nickname 익명시 익명으로 표시됨")
    private final String nickname;

    @Schema(description = "게시물 내 이미지 url , blank 가능", example = "example/1231")
    private final List<String> postImageUrl;

    @Schema(description = "게시물 익명 여부 default = true (익명)", example = "true")
    @NotNull
    private final boolean isAnonymous;

    @Schema(description = "좋아요 count", example = "0")
    private final int likeCount;

    @Schema(description = "스크랩 count", example = "0")
    private final int scrapCount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    @Schema(description = "생성 일자", example = "2024-12-02 20:10:18")
    private final LocalDateTime createdAt;

    public PostDetailResponseDTO(String userId, String _id, String title ,String content, String nickname, PostCategory postCategory, List<String> postImageUrls , boolean isAnonymous, int likeCount, int scrapCount, LocalDateTime createdAt) {
        this.userId = userId;
        this._id = _id;
        this.title = title;
        this.content = content;
        this.nickname = nickname;
        this.postCategory = postCategory;
        this.postImageUrl = postImageUrls;
        this.isAnonymous = isAnonymous;
        this.likeCount = likeCount;
        this.scrapCount = scrapCount;
        this.createdAt = createdAt;
    }
}
