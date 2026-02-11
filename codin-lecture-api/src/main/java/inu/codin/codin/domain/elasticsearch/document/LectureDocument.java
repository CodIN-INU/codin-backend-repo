package inu.codin.codin.domain.elasticsearch.document;

import inu.codin.codin.domain.elasticsearch.document.dto.EmotionInfo;
import inu.codin.codin.domain.elasticsearch.document.dto.ScheduleInfo;
import inu.codin.codin.domain.elasticsearch.document.dto.SemesterInfo;
import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class LectureDocument {
    private Long id;
    private String lectureName;
    private Integer grade;
    private Integer credit;
    private String professor;
    private String department;
    private String type;
    private String lectureType;
    private String evaluation;
    private List<String> preCourses;
    private Double starRating;
    private Integer likes;
    private Integer hits;
    private List<SemesterInfo> semesters;
    private List<String> tags;
    private List<ScheduleInfo> schedule;
    private EmotionInfo emotion;
    private String syllabus;
    private String aiSummary;
}
