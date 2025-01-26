package inu.codin.codin.domain.lecture.domain.review.dto;


import inu.codin.codin.domain.lecture.domain.review.entity.ReviewEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ReviewListResposneDto {
    private String _id;
    private String lectureId;
    private String userId;
    private String content;
    private double starRating;
    private int likes;
    private boolean isLiked;
    private String semester;

    @Builder
    public ReviewListResposneDto(String _id, String lectureId, String userId, String content, double starRating, int likes, boolean isLiked, String semester) {
        this._id = _id;
        this.lectureId = lectureId;
        this.userId = userId;
        this.content = content;
        this.starRating = starRating;
        this.likes = likes;
        this.isLiked = isLiked;
        this.semester = semester;
    }

    public static ReviewListResposneDto of(ReviewEntity reviewEntity, boolean isLiked, int likes){
        return ReviewListResposneDto.builder()
                ._id(reviewEntity.get_id().toString())
                .lectureId(reviewEntity.getLectureId().toString())
                .userId(reviewEntity.getUserId().toString())
                .content(reviewEntity.getContent())
                .starRating(reviewEntity.getStarRating())
                .likes(likes)
                .isLiked(isLiked)
                .semester(reviewEntity.getSemester())
                .build();
    }
}
