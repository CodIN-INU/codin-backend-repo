package inu.codin.codin.domain.elasticsearch.indexer;

import inu.codin.codin.domain.elasticsearch.document.LectureDocument;
import inu.codin.codin.domain.elasticsearch.repository.LectureElasticRepository;
import inu.codin.codin.domain.lecture.entity.Lecture;
import inu.codin.codin.domain.lecture.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LectureStartupIndexer {

    private final LectureElasticRepository lectureElasticRepository;
    private final LectureRepository lectureRepository;

    @Lazy
    private final LectureStartupIndexer self;

    private final int CHUNK_SIZE = 100;

    @Value("${elasticsearch.indexer.enabled:true}")
    private boolean indexerEnabled;

    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 환경 변수로 설정 적용
        if (!indexerEnabled) {
            log.info("ElasticSearch indexer 사용할 수 없습니다.");
            return;
        }

        // 기존 인덱스 존재 여부 확인
        if (lectureElasticRepository.count() > 0) {
            log.info("ElasticSearch 인덱스가 이미 존재합니다, 인덱싱을 생략합니다.");
            return;
        }

        // self injection으로 프록시 객체 참조
        self.indexAllLectures();
    }

    @Transactional(readOnly = true)
    public void indexAllLectures() {
        log.info("Starting ElasticSearch indexing 모든 Lecture 데이터");

        Pageable pageable = PageRequest.of(0, CHUNK_SIZE);
        Page<Lecture> page;
        int totalProcessed = 0;

        do {
            page = lectureRepository.findAllWithAssociations(pageable);
            List<LectureDocument> documents = page.getContent().stream()
                    .map(this::convertToDocument)
                    .toList();

            lectureElasticRepository.saveAll(documents);
            totalProcessed += documents.size();

            log.info("[indexAllLectures] Indexed Lecture: {}, Total: {})", documents.size(), totalProcessed);
            pageable = page.nextPageable();
        } while (page.hasNext());

        log.info("ElasticSearch 인덱싱 성공. Total Indexed: {}", totalProcessed);
    }

    private LectureDocument convertToDocument(Lecture lecture) {
        return LectureDocument.builder()
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
                .preCourses(lecture.getPreCourse() != null ? List.of(lecture.getPreCourse()) : List.of())
                .tags(lecture.getTags().stream()
                        .map(tag -> tag.getTag().getTagName())
                        .toList())
                .semesters(lecture.getSemester().stream()
                        .map(ls -> ls.getSemester().getString())
                        .toList())
                .build();
    }
}
