package inu.codin.codin.domain.lecture.dto;

import inu.codin.codin.domain.lecture.entity.LectureEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LectureListResponseDto {

    private String _id;
    private String lectureNm;
    private String professor;

    private double starRating;
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
