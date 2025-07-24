package inu.codin.codin.domain.review.entity;

import inu.codin.codin.domain.lecture.entity.Lecture;
import inu.codin.codin.domain.lecture.entity.LectureSemester;
import inu.codin.codin.domain.lecture.entity.Semester;
import inu.codin.codin.domain.review.dto.CreateReviewRequestDto;
import inu.codin.codin.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Review extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String content;
    private double starRating;
    private String userId;
    private int likes = 0;

    @ManyToOne
    @JoinColumn(name = "lecture_semester_id")
    private LectureSemester lectureSemester;

    @Builder
    public Review(String content, double starRating, String userId, LectureSemester lectureSemester) {
        this.content = content;
        this.starRating = starRating;
        this.userId = userId;
    }

    public static Review of(CreateReviewRequestDto createReviewRequestDto, String userId, LectureSemester lectureSemester) {
        return Review.builder()
                .content(createReviewRequestDto.getContent())
                .starRating(createReviewRequestDto.getStarRating())
                .userId(userId)
                .lectureSemester(lectureSemester)
                .build();
    }

    public void increaseLikes() {
        this.likes++;
    }

    public void decreaseLikes() {
        this.likes--;
    }
}
