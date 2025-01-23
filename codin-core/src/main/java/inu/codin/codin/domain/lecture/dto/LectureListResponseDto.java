package inu.codin.codin.domain.lecture.dto;

import inu.codin.codin.domain.lecture.entity.LectureEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

@Setter
@Getter
public class LectureListResponseDto {

    private String _id;
    private String lectureNm;
    private String lectureCode;
    private String professor;

    private int starRating;
    private int participants;

    @Builder
    public LectureListResponseDto(String _id, String lectureNm, String lectureCode, String professor, int starRating, int participants) {
        this._id = _id;
        this.lectureNm = lectureNm;
        this.lectureCode = lectureCode;
        this.professor = professor;
        this.starRating = starRating;
        this.participants = participants;
    }

    public static LectureListResponseDto of(LectureEntity lectureEntity){
        return LectureListResponseDto.builder()
                ._id(lectureEntity.get_id().toString())
                .lectureNm(lectureEntity.getLectureNm())
                .lectureCode(lectureEntity.getLectureCode())
                .professor(lectureEntity.getProfessor())
//                .starRating()
//                .participants()
                .build();
    }
}
