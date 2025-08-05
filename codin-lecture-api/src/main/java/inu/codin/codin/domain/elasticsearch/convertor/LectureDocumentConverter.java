package inu.codin.codin.domain.elasticsearch.convertor;

import inu.codin.codin.domain.elasticsearch.document.LectureDocument;
import inu.codin.codin.domain.lecture.entity.Lecture;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LectureDocumentConverter {

    public LectureDocument convertToDocument(Lecture lecture) {
        return LectureDocument.builder()
                .id(lecture.getId())
                .lectureNm(lecture.getLectureNm())
                .grade(lecture.getGrade())
                .department(lecture.getDepartment() != null ? lecture.getDepartment().name() : null)
                .starRating(lecture.getStarRating())
                .likes((long) lecture.getLikes())
                .hits((long) lecture.getHits())
                .professor(lecture.getProfessor())
                .type(lecture.getType() != null ? lecture.getType().name() : null)
                .lectureType(lecture.getLectureType())
                .evaluation(lecture.getEvaluation() != null ? lecture.getEvaluation().name() : null)
                .preCourses(lecture.getPreCourse() != null ? List.of(lecture.getPreCourse()) : List.of())
                .tags(lecture.getTags() != null ? lecture.getTags().stream()
                        .map(tag -> tag.getTag().getTagName())
                        .toList() : List.of())
                .semesters(lecture.getSemester() != null ? lecture.getSemester().stream()
                        .map(ls -> ls.getSemester().getString())
                        .toList() : List.of())
                .build();
    }
}
