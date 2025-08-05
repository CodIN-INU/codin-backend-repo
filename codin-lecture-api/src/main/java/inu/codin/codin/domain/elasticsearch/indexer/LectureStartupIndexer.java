package inu.codin.codin.domain.elasticsearch.indexer;

import inu.codin.codin.domain.elasticsearch.convertor.LectureDocumentConverter;
import inu.codin.codin.domain.elasticsearch.document.LectureDocument;
import inu.codin.codin.domain.elasticsearch.repository.LectureElasticRepository;
import inu.codin.codin.domain.lecture.entity.Lecture;
import inu.codin.codin.domain.lecture.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LectureStartupIndexer {

    private final LectureElasticRepository lectureElasticRepository;
    private final LectureRepository lectureRepository;
    private final LectureDocumentConverter lectureDocumentConverter;
    private final ElasticsearchOperations elasticsearchOperations; // 추가

    private final int CHUNK_SIZE = 100;

    @Value("${elasticsearch.indexer.enabled:true}")
    private boolean indexerEnabled;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 환경 변수로 설정 적용
        if (!indexerEnabled) {
            log.info("ElasticSearch indexer 사용할 수 없습니다.");
            return;
        }

        // 인덱스 존재 여부 확인 및 생성
        IndexOperations indexOps = elasticsearchOperations.indexOps(LectureDocument.class);
        if (!indexOps.exists()) {
            log.info("ElasticSearch lectures 인덱스가 존재하지 않아 직접 생성합니다.");
            indexOps.create();
            indexOps.putMapping(indexOps.createMapping(LectureDocument.class));
            log.info("ElasticSearch lectures 인덱스 및 매핑 생성 완료.");
        }

        long existingCount = lectureElasticRepository.count();
        if (existingCount > 0) {
            log.info("ElasticSearch 인덱스가 이미 존재합니다 ({}개), 전체 재인덱싱을 진행합니다.", existingCount);
            // 선택적으로 기존 인덱스 삭제 후 재인덱싱
            lectureElasticRepository.deleteAll();
            log.info("기존 인덱스를 삭제했습니다.");
        }

        log.info("Starting ElasticSearch Indexing, Lecture Data");
        int pageNumber = 0;
        int totalProcessed = 0;
        List<Lecture> lectures;

        do {
            Pageable pageable = PageRequest.of(pageNumber, CHUNK_SIZE);
            lectures = lectureRepository.findAllPaged(pageable);

            if (!lectures.isEmpty()) {
                List<LectureDocument> documents = lectures.stream()
                        .map(lectureDocumentConverter::convertToDocument)
                        .toList();

                lectureElasticRepository.saveAll(documents);
                totalProcessed += documents.size();

                log.info("[indexAllLectures] Indexed Lecture: {}, Total: {}", documents.size(), totalProcessed);
            }

            pageNumber++;
        } while (lectures.size() == CHUNK_SIZE);

        log.info("ElasticSearch 인덱싱 성공. Total Indexed: {}", totalProcessed);
    }
}
