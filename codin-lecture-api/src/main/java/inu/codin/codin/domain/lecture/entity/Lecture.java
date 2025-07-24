package inu.codin.codin.domain.lecture.entity;

import inu.codin.codin.domain.lecture.converter.EvaluationConverter;
import inu.codin.codin.domain.lecture.converter.TypeConverter;
import inu.codin.codin.domain.review.entity.Review;
import inu.codin.codin.global.common.entity.Department;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Lecture {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String lectureNm;
    private int grade; //0 : 전학년
    private int credit;
    private String professor;

    @Enumerated(EnumType.STRING)
    private Department department; //OTHERS : 교양

    @Convert(converter = TypeConverter.class)
    private Type type;
    private String lectureType;

    @Convert(converter = EvaluationConverter.class)
    private Evaluation evaluation;
    private String preCourse;
    private double starRating;
    private int likes;
    private int hits;

    @OneToOne
    private Emotion emotion = new Emotion();

    @OneToMany(mappedBy = "lecture", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<LectureSemester> semester;

    @OneToMany(mappedBy = "lecture", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<LectureTag> tags;

    @OneToMany(mappedBy = "lecture", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LectureSchedule> schedule;

    @OneToMany(mappedBy = "lecture", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;

    public void increaseLikes() { this.likes++; }

    public void decreaseLikes() { this.likes--; };

    public void increaseHits(){
        this.hits++;
    }

    public void updateReviewRating(double starRating, Emotion emotion) {
        this.starRating = starRating;
        this.emotion = emotion;
    }
}
