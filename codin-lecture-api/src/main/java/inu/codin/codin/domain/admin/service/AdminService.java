package inu.codin.codin.domain.admin.service;

import inu.codin.codin.domain.elasticsearch.indexer.LectureStartupIndexer;
import inu.codin.codin.domain.lecture.repository.LectureRepository;
import inu.codin.codin.domain.lecture.service.LectureSummarizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminService {

    private final LectureStartupIndexer indexer;

    private final LectureRepository lectureRepository;
    private final LectureSummarizationService lectureSummarizationService;

    @Async
    @Transactional
    public void reindexAllLectures() {
        log.info("[reIndexAllLectures] 모든 과목을 다시 ES에 재 인덱싱합니다.");
        indexer.lectureIndex();
    }

    @Async
    @Transactional
    public void reSummarizeAllLectures() {
        log.info("[reSummarizeAllLectures] 모든 교과목을 다시 AI 요약본을 생성합니다.");
        List<Long> lectureIds = lectureRepository.findAllLectureIds();

        log.info("[processLecturesWithoutAiSummary] AI 요약이 없는 강의 수: {}", lectureIds.size());
        for (Long lectureId : lectureIds) {
            try {
                lectureSummarizationService.summarizeLecture(lectureId);
                log.debug("[processLecturesWithoutAiSummary] 강의 ID {} AI 요약 생성 완료", lectureId);
                Thread.sleep(500);
            } catch (Exception e) {
                log.error("[processLecturesWithoutAiSummary] 강의 ID {} AI 요약 생성 실패", lectureId, e);
            }
        }
    }
}
