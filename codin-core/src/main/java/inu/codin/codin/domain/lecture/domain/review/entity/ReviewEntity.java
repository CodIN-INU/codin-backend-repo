package inu.codin.codin.domain.lecture.domain.review.entity;

import inu.codin.codin.common.dto.BaseTimeEntity;
import inu.codin.codin.domain.lecture.domain.review.dto.CreateReviewRequestDto;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "reviews")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ReviewEntity extends BaseTimeEntity {
    private ObjectId _id;
    private ObjectId lectureId;
    private ObjectId userId;
    private String content;
    private double starRating;
    private String semester;

    @Builder
    public ReviewEntity(ObjectId _id, ObjectId lectureId, ObjectId userId, String content, double starRating, String semester) {
        this._id = _id;
        this.lectureId = lectureId;
        this.userId = userId;
        this.content = content;
        this.starRating = starRating;
        this.semester = semester;
    }

    public static ReviewEntity of(CreateReviewRequestDto createReviewRequestDto, ObjectId lectureId, ObjectId userId){
        return ReviewEntity.builder()
                .content(createReviewRequestDto.getContent())
                .starRating(createReviewRequestDto.getStarRating())
                .lectureId(lectureId)
                .userId(userId)
                .semester(createReviewRequestDto.getSemester())
                .build();
    }
}
