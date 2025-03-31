package inu.codin.codin.domain.lecture.dto;

import inu.codin.codin.domain.lecture.entity.LectureEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class LectureListResponseDto {

    @Schema(description = "LectureEntity _id", example = "1111111")
    private String _id;

    @Schema(description = "강의명", example = "Java")
    private String lectureNm;

    @Schema(description = "교수명", example = "홍길동")
    private String professor;

    @Schema(description = "강의 평점 평균", example = "2.5")
    private double starRating;

    @Schema(description = "수강 후기 작성자 수", example = "10")
    private int participants;

    @Schema(description = "해당 강의가 열린 학기들", example = "23-1, 24-1, 25-1")
    private List<String> semesters;

    @Schema(description = "학년", example = "2")
    private int grade;

    @Builder
    public LectureListResponseDto(String _id, String lectureNm, String professor, double starRating, int participants, List<String> semesters, int grade) {
        this._id = _id;
        this.lectureNm = lectureNm;
        this.professor = professor;
        this.starRating = starRating;
        this.participants = participants;
        this.semesters = semesters;
        this.grade = grade;
    }

    public static LectureListResponseDto of(LectureEntity lectureEntity){
        return LectureListResponseDto.builder()
                ._id(lectureEntity.get_id().toString())
                .lectureNm(lectureEntity.getLectureNm())
                .professor(lectureEntity.getProfessor())
                .starRating(lectureEntity.getStarRating())
                .participants(lectureEntity.getParticipants())
                .semesters(lectureEntity.getSemester())
                .grade(lectureEntity.getGrade())
                .build();
    }
}
