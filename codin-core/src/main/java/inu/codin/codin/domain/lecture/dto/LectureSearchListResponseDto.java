package inu.codin.codin.domain.lecture.dto;

import inu.codin.codin.domain.lecture.entity.LectureEntity;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class LectureSearchListResponseDto {

    private String _id;
    private String lectureNm;
    private String professor;
    private String semester;

    @Builder
    public LectureSearchListResponseDto(String _id, String lectureNm, String professor, String semester) {
        this._id = _id;
        this.lectureNm = lectureNm;
        this.professor = professor;
        this.semester = semester;
    };


    public static List<LectureSearchListResponseDto> of(LectureEntity lecture) {
        List<LectureSearchListResponseDto> listResponseDtos = new ArrayList<>();
        for (String semester: lecture.getSemester()){
            listResponseDtos.add(LectureSearchListResponseDto.builder()
                    ._id(lecture.get_id().toString())
                    .lectureNm(lecture.getLectureNm())
                    .professor(lecture.getProfessor())
                    .semester(semester)
                    .build());
        }
        return listResponseDtos;
    }

    public static LectureSearchListResponseDto of(LectureEntity lecture, String semester) {
            return LectureSearchListResponseDto.builder()
                    ._id(lecture.get_id().toString())
                    .lectureNm(lecture.getLectureNm())
                    .professor(lecture.getProfessor())
                    .semester(semester)
                    .build();
    }
}
