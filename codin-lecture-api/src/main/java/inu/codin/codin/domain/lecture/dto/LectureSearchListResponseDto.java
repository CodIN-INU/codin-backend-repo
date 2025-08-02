package inu.codin.codin.domain.lecture.dto;

import inu.codin.codin.domain.lecture.entity.Lecture;
import inu.codin.codin.domain.lecture.entity.LectureSemester;
import inu.codin.codin.domain.lecture.entity.Semester;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class LectureSearchListResponseDto {

    private Long id;
    private String lectureNm;
    private String professor;
    private String semester;

    @Builder
    public LectureSearchListResponseDto(Long id, String lectureNm, String professor, String semester) {
        this.id = id;
        this.lectureNm = lectureNm;
        this.professor = professor;
        this.semester = semester;
    }


    public static List<LectureSearchListResponseDto> of(Lecture lecture) {
        List<LectureSearchListResponseDto> listResponseDtos = new ArrayList<>();
        for (LectureSemester semester: lecture.getSemester()){
            listResponseDtos.add(LectureSearchListResponseDto.builder()
                    .id(lecture.getId())
                    .lectureNm(lecture.getLectureNm())
                    .professor(lecture.getProfessor())
                    .semester(semester.getSemester().getString())
                    .build());
        }
        return listResponseDtos;
    }

    public static LectureSearchListResponseDto of(Lecture lecture, String semester) {
            return LectureSearchListResponseDto.builder()
                    .id(lecture.getId())
                    .lectureNm(lecture.getLectureNm())
                    .professor(lecture.getProfessor())
                    .semester(semester)
                    .build();
    }
}
