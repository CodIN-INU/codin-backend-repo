package inu.codin.codin.domain.review.dto;


import inu.codin.codin.domain.review.entity.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ReviewListResponseDto {

    @Schema(description = "Review pk", example = "1")
    private final Long id;

    @Schema(description = "수강 후기 내용", example = "완전 강추")
    private final String content;

    @Schema(description = "수강 후기 평점")
    private final double starRating;

    @Schema(description = "좋아요 수", example = "3")
    private final int likeCount;

    @Schema(description = "유저의 좋아요 반영 여부", example = "true")
    private final boolean isLiked;

    @Schema(description = "수강 학기", example = "24-2")
    private final String semester;

    @Schema(description = "좋아요 수", example = "24")
    private final int likes;

    @Builder
    public ReviewListResponseDto(Long id, String content, double starRating, int likeCount, boolean isLiked, String semester, int likes) {
        this.id = id;
        this.content = content;
        this.starRating = starRating;
        this.likeCount = likeCount;
        this.isLiked = isLiked;
        this.semester = semester;
        this.likes = likes;
    }

    public static ReviewListResponseDto of(Review reviewEntity, boolean isLiked, int likeCount){
        return ReviewListResponseDto.builder()
                .id(reviewEntity.getId())
                .content(reviewEntity.getContent())
                .starRating(reviewEntity.getStarRating())
                .likeCount(likeCount)
                .isLiked(isLiked)
                .semester(reviewEntity.getSemester().getString())
                .likes(reviewEntity.getLikes())
                .build();
    }
}
