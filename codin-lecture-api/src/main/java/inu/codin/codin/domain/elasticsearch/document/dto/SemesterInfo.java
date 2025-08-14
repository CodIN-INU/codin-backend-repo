package inu.codin.codin.domain.elasticsearch.document.dto;

import inu.codin.codin.domain.lecture.entity.LectureSemester;
import inu.codin.codin.domain.lecture.entity.Semester;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Builder
public class SemesterInfo {
    @Field(type = FieldType.Integer)
    private Integer year;

    @Field(type = FieldType.Integer)
    private Integer quarter;

    public static SemesterInfo from(LectureSemester lectureSemester) {
        Semester semester = lectureSemester.getSemester();

        return SemesterInfo.builder()
                .year(semester.getYear())
                .quarter(semester.getQuarter())
                .build();
    }
}
