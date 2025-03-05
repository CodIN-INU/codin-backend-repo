package inu.codin.codin.domain.lecture.domain.review.dto;


import inu.codin.codin.domain.lecture.domain.review.entity.ReviewEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ReviewListResposneDto {

    @Schema(description = "ReviewEntity _id", example = "1111111")
    private String _id;

    @Schema(description = "수강 후기를 단 강의 _id", example = "2222222")
    private String lectureId;

    @Schema(description = "수강 후기 작성한 유저 _id", example = "3333333")
    private String userId;

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
    public ReviewListResposneDto(String _id, String lectureId, String userId, String content, double starRating, int likeCount, boolean isLiked, String semester) {
        this._id = _id;
        this.lectureId = lectureId;
        this.userId = userId;
        this.content = content;
        this.starRating = starRating;
        this.likeCount = likeCount;
        this.isLiked = isLiked;
        this.semester = semester;
    }

    public static ReviewListResposneDto of(ReviewEntity reviewEntity, boolean isLiked, int likeCount){
        return ReviewListResposneDto.builder()
                ._id(reviewEntity.get_id().toString())
                .lectureId(reviewEntity.getLectureId().toString())
                .userId(reviewEntity.getUserId().toString())
                .content(reviewEntity.getContent())
                .starRating(reviewEntity.getStarRating())
                .likeCount(likeCount)
                .isLiked(isLiked)
                .semester(reviewEntity.getSemester())
                .build();
    }
}
