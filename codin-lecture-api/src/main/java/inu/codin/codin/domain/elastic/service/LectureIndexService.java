package inu.codin.codin.domain.elastic.service;

import inu.codin.codin.domain.elastic.document.LectureDocument;
import inu.codin.codin.domain.elastic.event.LectureDeletedEvent;
import inu.codin.codin.domain.elastic.event.LectureSavedEvent;
import inu.codin.codin.domain.elastic.reposiory.LectureElasticRepository;
import inu.codin.codin.domain.lecture.entity.Lecture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class LectureIndexService {

    private final LectureElasticRepository lectureElasticRepository;

    // todo: 또는 엔티티 콜백 인터페이스 구현 방법도 있음
    // BeforeConvertCallback, AfterSaveCallback, AfterDeleteCallback...

    /**
     * 강의 저장/수정 후 호출
     * ApplicationEventPublisher -> LectureSavedEvent
     * @param event LectureSavedEvent
     */
    @TransactionalEventListener(classes = {LectureSavedEvent.class})
    public void handleLectureSaved(LectureSavedEvent event) {
        Lecture lecture = event.getLecture();

        LectureDocument document = LectureDocument.builder()
                .id(lecture.getId())
                .lectureNm(lecture.getLectureNm())
                .professor(lecture.getProfessor())
                .department(lecture.getDepartment().name())
                .grade(lecture.getGrade())
                .semesters(
                        lecture.getSemester()
                                .stream()
                                .map((lectureSemester -> lectureSemester.getSemester().getString()))  // 엔티티에서 학기 문자열 얻어오기
                                .toList()
                )
                .starRating(lecture.getStarRating())
                .likes((long) lecture.getLikes())
                .hits((long) lecture.getHits())
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
