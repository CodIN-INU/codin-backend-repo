package inu.codin.codin.domain.elasticsearch.indexer;

import inu.codin.codin.domain.elasticsearch.convertor.LectureDocumentConverter;
import inu.codin.codin.domain.elasticsearch.document.LectureDocument;
import inu.codin.codin.domain.elasticsearch.repository.LectureElasticRepository;
import inu.codin.codin.domain.lecture.entity.Lecture;
import inu.codin.codin.domain.lecture.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LectureStartupIndexer {

    private final LectureRepository lectureRepository;
    private final LectureDocumentConverter lectureDocumentConverter;
    private final LectureElasticRepository lectureElasticRepository;

    public void lectureIndex() {
        manageIndex();
        long totalProcessed = performIndexing();

        log.info("ElasticSearch 인덱싱 성공. Total Indexed: {}", totalProcessed);
    }

    private void manageIndex() {
        try {
            boolean isExistIndex = lectureElasticRepository.isLectureIndexExist();

            if (isExistIndex) {
                log.info("ElasticSearch lectures 문서 전체 재색인을 위해 인덱스를 삭제합니다.");
                lectureElasticRepository.deleteLectureIndex();
            }

            log.info("ElasticSearch lectures 인덱스를 생성합니다.");
            lectureElasticRepository.createLectureIndex();
            log.info("ElasticSearch lectures 인덱스 및 매핑 생성 완료.");
        } catch (IOException e) {
            log.error("인덱스 생성에 실패했습니다. : {}", e.getMessage());
        }
    }

    private long performIndexing() {
        log.info("Lecture 문서 색인을 시작합니다.");
        List<Lecture> lectures = lectureRepository.findAll();

        if (!lectures.isEmpty()) {
            List<LectureDocument> documents = lectures.stream()
                    .map(lectureDocumentConverter::convertToDocument)
                    .toList();

            lectureElasticRepository.bulkIndexLectures(documents);
        }

        return lectures.size();
    }
}
