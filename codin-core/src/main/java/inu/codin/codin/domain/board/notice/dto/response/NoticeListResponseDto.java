package inu.codin.codin.domain.board.notice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import inu.codin.codin.domain.post.entity.PostCategory;
import inu.codin.codin.domain.post.entity.PostEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NoticeListResponseDto {
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

    @Schema(description = "게시물 내 대표 이미지 하나 url , null 가능", example = "example/1231")
    private final String postImageUrl;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    @Schema(description = "생성 일자", example = "2024-12-02 20:10:18")
    private final LocalDateTime createdAt;

    @Builder
    public NoticeListResponseDto(String _id, String userId, PostCategory postCategory, String title, String content, String nickname, String postImageUrl, LocalDateTime createdAt) {
        this._id = _id;
        this.userId = userId;
        this.postCategory = postCategory;
        this.title = title;
        this.content = content;
        this.nickname = nickname;
        this.postImageUrl = postImageUrl;
        this.createdAt = createdAt;
    }

    public static NoticeListResponseDto of(PostEntity postEntity, String nickname) {
        return NoticeListResponseDto.builder()
                ._id(postEntity.get_id().toString())
                .userId(String.valueOf(postEntity.getUserId()))
                .postCategory(postEntity.getPostCategory())
                .title(postEntity.getTitle())
                .content(postEntity.getContent())
                .nickname(nickname)
                .postImageUrl(postEntity.getPostImageUrls() != null && !postEntity.getPostImageUrls().isEmpty() ? postEntity.getPostImageUrls().get(0) : null)
                .createdAt(postEntity.getCreatedAt())
                .build();
    }
}
