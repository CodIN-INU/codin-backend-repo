package inu.codin.codin.domain.lecture.domain.review.dto;


import inu.codin.codin.domain.lecture.domain.review.entity.ReviewEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ReviewListResposneDto {
    private String _id;
    private String lectureId;
    private String userId;
    private String title;
    private String content;
    private double starRating;
    private int likes;

    private boolean isLiked;

    @Builder
    public ReviewListResposneDto(String _id, String lectureId, String userId, String title, String content, double starRating, int likes, boolean isLiked) {
        this._id = _id;
        this.lectureId = lectureId;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.starRating = starRating;
        this.likes = likes;
        this.isLiked = isLiked;
    }

    public static ReviewListResposneDto of(ReviewEntity reviewEntity, boolean isLiked, int likes){
        return ReviewListResposneDto.builder()
                ._id(reviewEntity.get_id().toString())
                .lectureId(reviewEntity.getLectureId().toString())
                .userId(reviewEntity.getUserId().toString())
                .title(reviewEntity.getTitle())
                .content(reviewEntity.getContent())
                .starRating(reviewEntity.getStarRating())
                .likes(likes)
                .isLiked(isLiked)
                .build();
    }
}
