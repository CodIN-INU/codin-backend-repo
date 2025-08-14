package inu.codin.codin.domain.elasticsearch.indexer;

import inu.codin.codin.domain.elasticsearch.convertor.LectureDocumentConverter;
import inu.codin.codin.domain.elasticsearch.document.LectureDocument;
import inu.codin.codin.domain.elasticsearch.repository.LectureElasticRepository;
import inu.codin.codin.domain.lecture.entity.Lecture;
import inu.codin.codin.domain.lecture.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

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

    public void lectureIndex() {
        if (!indexerEnabled) {
            log.info("ElasticSearch indexer 사용할 수 없습니다.");

            return;
        }

        manageIndex();
        long totalProcessed = performIndexing();

        log.info("ElasticSearch 인덱싱 성공. Total Indexed: {}", totalProcessed);
    }

    private void manageIndex() {
        IndexOperations indexOps = elasticsearchOperations.indexOps(LectureDocument.class);
        if (indexOps.exists()) {
            log.info("ElasticSearch lectures 문서 전체 재색인을 위해 인덱스를 삭제합니다.");
            indexOps.delete();
        }

        log.info("ElasticSearch lectures 인덱스를 생성합니다.");
        indexOps.create();
        indexOps.putMapping(indexOps.createMapping(LectureDocument.class));
        log.info("ElasticSearch lectures 인덱스 및 매핑 생성 완료.");
    }

    private long performIndexing() {
        log.info("Lecture 문서 색인을 시작합니다.");

        int pageNumber = 0;
        long totalProcessed = 0L;
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

        elasticsearchOperations.indexOps(LectureDocument.class).refresh();

        return totalProcessed;
    }
}
