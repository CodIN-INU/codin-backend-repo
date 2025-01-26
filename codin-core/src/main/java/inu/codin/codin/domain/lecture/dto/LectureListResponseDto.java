package inu.codin.codin.domain.lecture.dto;

import inu.codin.codin.domain.lecture.entity.LectureEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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
    private long participants;

    @Builder
    public LectureListResponseDto(String _id, String lectureNm, String professor, double starRating, long participants) {
        this._id = _id;
        this.lectureNm = lectureNm;
        this.professor = professor;
        this.starRating = starRating;
        this.participants = participants;
    }

    public static LectureListResponseDto of(LectureEntity lectureEntity, double starRating, long participants){
        return LectureListResponseDto.builder()
                ._id(lectureEntity.get_id().toString())
                .lectureNm(lectureEntity.getLectureNm())
                .professor(lectureEntity.getProfessor())
                .starRating(starRating)
                .participants(participants)
                .build();
    }
}
