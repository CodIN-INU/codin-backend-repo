package inu.codin.codin.domain.elasticsearch.service;

import inu.codin.codin.domain.elasticsearch.document.LectureDocument;
import inu.codin.codin.domain.elasticsearch.event.LectureDeletedEvent;
import inu.codin.codin.domain.elasticsearch.event.LectureSavedEvent;
import inu.codin.codin.domain.elasticsearch.repository.LectureElasticRepository;
import inu.codin.codin.domain.lecture.entity.Lecture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LectureIndexService {

    private final LectureElasticRepository lectureElasticRepository;

    // todo: 또는 엔티티 콜백 인터페이스 구현 방법도 있음
    // BeforeConvertCallback, AfterSaveCallback, AfterDeleteCallback...

    /**
     * 강의 저장 / 수정 후 호출
     * ApplicationEventPublisher -> LectureSavedEvent
     * @param event LectureSavedEvent
     */
    @TransactionalEventListener(classes = {LectureSavedEvent.class})
    public void handleLectureSaved(LectureSavedEvent event) {
        Lecture lecture = event.getLecture();

        LectureDocument document = LectureDocument.builder()
                .id(lecture.getId())
                .lectureNm(lecture.getLectureNm())
                .grade(lecture.getGrade())
                .department(lecture.getDepartment().name())
                .starRating(lecture.getStarRating())
                .likes((long) lecture.getLikes())
                .hits((long) lecture.getHits())
                .professor(lecture.getProfessor())
                .type(lecture.getType().name())
                .lectureType(lecture.getLectureType())
                .evaluation(lecture.getEvaluation().name())
                .preCourses(List.of(lecture.getPreCourse()))
                .tags(lecture.getTags().stream()
                        .map(tag -> tag.getTag().getTagName())
                        .toList())
                .semesters(lecture.getSemester().stream()
                        .map(l -> l.getSemester().getString())
                        .toList())
                .build();
        lectureElasticRepository.save(document);
    }

    /**
     * 강의 삭제 시 호출
     * ApplicationEventPublisher -> LectureDeletedEvent
     * @param event LectureDeletedEvent
     */
    @TransactionalEventListener(classes = {LectureDeletedEvent.class})
    public void handleLectureDeleted(LectureDeletedEvent event) {
        lectureElasticRepository.deleteById(event.getLectureId());
    }
}
