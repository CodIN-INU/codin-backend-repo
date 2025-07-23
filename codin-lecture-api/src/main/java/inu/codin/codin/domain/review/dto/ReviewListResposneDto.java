package inu.codin.codin.domain.review.dto;


import inu.codin.codin.domain.review.entity.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ReviewListResposneDto {

    @Schema(description = "Review pk", example = "1")
    private String _id;

    @Schema(description = "수강 후기 내용", example = "완전 강추")
    private String content;

    @Schema(description = "수강 후기 평점")
    private double starRating;

    @Schema(description = "좋아요 수", example = "3")
    private int likeCount;

    @Schema(description = "유저의 좋아요 반영 여부", example = "true")
    private boolean isLiked;

    @Schema(description = "수강 학기", example = "24-2")
    private String semester;

    @Builder
    public ReviewListResposneDto(String _id, String content, double starRating, int likeCount, boolean isLiked, String semester) {
        this._id = _id;
        this.content = content;
        this.starRating = starRating;
        this.likeCount = likeCount;
        this.isLiked = isLiked;
        this.semester = semester;
    }

    public static ReviewListResposneDto of(Review reviewEntity, boolean isLiked, int likeCount){
        return ReviewListResposneDto.builder()
                ._id(reviewEntity.get_id().toString())
                .content(reviewEntity.getContent())
                .starRating(reviewEntity.getStarRating())
                .likeCount(likeCount)
                .isLiked(isLiked)
                .semester(reviewEntity.getSemester().getString())
                .build();
    }
}
