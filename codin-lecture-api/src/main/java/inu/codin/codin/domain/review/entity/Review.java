package inu.codin.codin.domain.review.entity;

import inu.codin.codin.domain.lecture.entity.Lecture;
import inu.codin.codin.domain.lecture.entity.Semester;
import inu.codin.codin.domain.review.dto.CreateReviewRequestDto;
import inu.codin.codin.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "reviews")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Review extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long _id;
    private String content;
    private double starRating;
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    @OneToOne
    @JoinColumn(name = "semester_id")
    private Semester semester;

    @Builder
    public Review(String content, double starRating, Semester semester, Long userId, Lecture lecture) {
        this.content = content;
        this.starRating = starRating;
        this.semester = semester;
        this.userId = userId;
        this.lecture = lecture;
    }

    public static Review of(CreateReviewRequestDto createReviewRequestDto, Semester semester, Lecture lecture, Long userId) {
        return Review.builder()
                .content(createReviewRequestDto.getContent())
                .starRating(createReviewRequestDto.getStarRating())
                .semester(semester)
                .userId(userId)
                .lecture(lecture)
                .build();
    }
}
