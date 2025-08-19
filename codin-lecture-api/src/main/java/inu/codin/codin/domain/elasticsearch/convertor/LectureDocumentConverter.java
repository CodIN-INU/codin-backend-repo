package inu.codin.codin.domain.elasticsearch.convertor;

import inu.codin.codin.domain.elasticsearch.document.LectureDocument;
import inu.codin.codin.domain.elasticsearch.document.dto.EmotionInfo;
import inu.codin.codin.domain.elasticsearch.document.dto.ScheduleInfo;
import inu.codin.codin.domain.elasticsearch.document.dto.SemesterInfo;
import inu.codin.codin.domain.lecture.entity.Lecture;
import inu.codin.codin.domain.lecture.entity.LectureSchedule;
import inu.codin.codin.domain.lecture.entity.LectureSemester;
import inu.codin.codin.domain.lecture.entity.LectureTag;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class LectureDocumentConverter {

    public LectureDocument convertToDocument(Lecture lecture) {
        return LectureDocument.builder()
                .id(lecture.getId())
                .lectureNm(lecture.getLectureNm())
                .grade(lecture.getGrade())
                .credit(lecture.getCredit())
                .professor(lecture.getProfessor())
                .department(lecture.getDepartment() != null ? lecture.getDepartment().name() : null)
                .type(lecture.getType() != null ? lecture.getType().name() : null)
                .lectureType(lecture.getLectureType())
                .evaluation(lecture.getEvaluation() != null ? lecture.getEvaluation().name() : null)
                .preCourses(toPreCourses(lecture.getPreCourse()))
                .starRating(lecture.getStarRating())
                .likes(lecture.getLikes())
                .hits(lecture.getHits())
                .semesters(toSemesterInfos(lecture.getSemester()))
                .tags(toTagNames(lecture.getTags()))
                .schedule(toScheduleInfos(lecture.getSchedule()))
                .emotion(EmotionInfo.from(lecture.getEmotion()))
                .syllabus(lecture.getSyllabus().getFullText())
                .aiSummary(lecture.getAiSummary())
                .build();
    }

    private List<String> toPreCourses(String preCourse) {
        if (preCourse == null || preCourse.isBlank()) {
            return List.of();
        }

        return List.of(preCourse.split(",\\s*")); // 쉼표 기준으로 분리하는 예시
    }

    private List<SemesterInfo> toSemesterInfos(Set<LectureSemester> semesters) {
        if (semesters == null) {
            return List.of();
        }

        return semesters.stream()
                .map(SemesterInfo::from)
                .toList();
    }

    private List<String> toTagNames(Set<LectureTag> tags) {
        if (tags == null) {
            return List.of();
        }

        return tags.stream()
                .map(lectureTag -> lectureTag.getTag().getTagName())
                .toList();
    }

    private List<ScheduleInfo> toScheduleInfos(Set<LectureSchedule> schedules) {
        if (schedules == null) {
            return List.of();
        }

        return schedules.stream()
                .map(ScheduleInfo::from)
                .toList();
    }
}
