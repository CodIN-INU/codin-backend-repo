package inu.codin.codin.domain.elasticsearch.service;

import inu.codin.codin.domain.elasticsearch.convertor.LectureDocumentConverter;
import inu.codin.codin.domain.elasticsearch.document.LectureDocument;
import inu.codin.codin.domain.elasticsearch.event.LectureDeletedEvent;
import inu.codin.codin.domain.elasticsearch.event.LectureSavedEvent;
import inu.codin.codin.domain.elasticsearch.repository.LectureElasticRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class LectureIndexService {

    private final LectureElasticRepository lectureElasticRepository;

    private final LectureDocumentConverter lectureDocumentConverter;

    // todo: 또는 엔티티 콜백 인터페이스 구현 방법도 있음
    // BeforeConvertCallback, AfterSaveCallback, AfterDeleteCallback...

    /**
     * 강의 저장 / 수정 후 호출
     * ApplicationEventPublisher -> LectureSavedEvent
     * @param event LectureSavedEvent
     */
    @TransactionalEventListener(classes = {LectureSavedEvent.class})
    public void handleLectureSaved(LectureSavedEvent event) {
        LectureDocument document = lectureDocumentConverter.convertToDocument(event.getLecture());
        try {
            lectureElasticRepository.saveLecture(document);
        } catch (IOException e) {
            log.error("강의 저장 실패, {}" , e.getMessage());
        }
    }

    /**
     * 강의 삭제 시 호출
     * ApplicationEventPublisher -> LectureDeletedEvent
     * @param event LectureDeletedEvent
     */
    @TransactionalEventListener(classes = {LectureDeletedEvent.class})
    public void handleLectureDeleted(LectureDeletedEvent event) {
        try {
            lectureElasticRepository.deleteLecture(event.lectureId());
        } catch (IOException e) {
            log.error("강의 삭제 실패, {}" , e.getMessage());
        }
    }
}
