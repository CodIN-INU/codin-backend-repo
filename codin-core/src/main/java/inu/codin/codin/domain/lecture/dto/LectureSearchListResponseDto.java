package inu.codin.codin.domain.lecture.dto;

import inu.codin.codin.domain.lecture.entity.LectureEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LectureSearchListResponseDto {

    private String _id;
    private String lectureNm;
    private String professor;

    @Builder
    public LectureSearchListResponseDto(String _id, String lectureNm, String professor) {
        this._id = _id;
        this.lectureNm = lectureNm;
        this.professor = professor;
    };


    public static LectureSearchListResponseDto of(LectureEntity lecture) {
        return LectureSearchListResponseDto.builder()
                ._id(lecture.get_id().toString())
                .lectureNm(lecture.getLectureNm())
                .professor(lecture.getProfessor())
                .build();
    }
}
