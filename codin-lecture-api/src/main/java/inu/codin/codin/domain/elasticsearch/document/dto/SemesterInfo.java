package inu.codin.codin.domain.elasticsearch.document.dto;

import inu.codin.codin.domain.lecture.entity.LectureSemester;
import inu.codin.codin.domain.lecture.entity.Semester;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class SemesterInfo {
    private Integer year;
    private Integer quarter;

    public static SemesterInfo from(LectureSemester lectureSemester) {
        Semester semester = lectureSemester.getSemester();

        return SemesterInfo.builder()
                .year(semester.getYear())
                .quarter(semester.getQuarter())
                .build();
    }
}
