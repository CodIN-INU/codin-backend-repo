package inu.codin.codin.domain.lecture.scheduler;

import inu.codin.codin.domain.lecture.repository.LectureRepository;
import inu.codin.codin.domain.lecture.service.LectureSummarizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LectureAiSummaryScheduler {

    private final LectureRepository lectureRepository;
    private final LectureSummarizationService summarizationService;

    /**
     * 매시간 AI 요약이 없는 강의들 처리 (00분마다 실행)
     */
    @Scheduled(cron = "0 0 * * * *")
    @Async("aiSummaryScheduleExecutor")
    public void generateMissingAiSummaries() {
        log.info("[generateMissingAiSummaries] 매시간 AI 요약 누락 강의 처리 시작");

        try {
            processLecturesWithoutAiSummary();
            log.info("[generateMissingAiSummaries] 매시간 AI 요약 누락 강의 처리 완료");
        } catch (Exception e) {
            log.error("[generateMissingAiSummaries] 매시간 AI 요약 누락 강의 처리 중 오류 발생", e);
        }
    }

    /**
     * 매일 새벽 3시 전체 AI 요약 생성/업데이트
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Async("aiSummaryScheduleExecutor")
    public void generateDailyAiSummaries() {
        log.info("[generateDailyAiSummaries] 일일 AI 요약 생성 작업 시작");

        try {
            // 최근 리뷰가 업데이트된 강의들 처리
            processLecturesWithRecentReviews();

            log.info("[generateDailyAiSummaries] 일일 AI 요약 생성 작업 완료");
        } catch (Exception e) {
            log.error("[generateDailyAiSummaries] 일일 AI 요약 생성 작업 중 오류 발생", e);
        }
    }

    /**
     * AI 요약이 없는 강의들 처리
     */
    private void processLecturesWithoutAiSummary() {
        List<Long> lectureIdsWithoutSummary = lectureRepository.findLectureIdsWithoutAiSummary();
        log.info("[processLecturesWithoutAiSummary] AI 요약이 없는 강의 수: {}", lectureIdsWithoutSummary.size());

        for (Long lectureId : lectureIdsWithoutSummary) {
            try {
                summarizationService.summarizeLecture(lectureId);
                log.debug("[processLecturesWithoutAiSummary] 강의 ID {} AI 요약 생성 완료", lectureId);
                Thread.sleep(500); // API 호출 제한 딜레이
            } catch (Exception e) {
                log.error("[processLecturesWithoutAiSummary] 강의 ID {} AI 요약 생성 실패", lectureId, e);
            }
        }
    }

    /**
     * 최근 리뷰가 업데이트된 강의들 처리
     */
    private void processLecturesWithRecentReviews() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        List<Long> lectureIdsWithRecentReviews = lectureRepository.findLectureIdsWithRecentReviews(yesterday);
        log.info("[processLecturesWithRecentReviews] 최근 리뷰가 등록된 강의 수: {}", lectureIdsWithRecentReviews.size());

        for (Long lectureId : lectureIdsWithRecentReviews) {
            try {
                summarizationService.summarizeLecture(lectureId);
                log.debug("[processLecturesWithRecentReviews] 강의 ID {} AI 요약 업데이트 완료", lectureId);
                Thread.sleep(500); // API 호출 제한 딜레이
            } catch (Exception e) {
                log.error("[processLecturesWithRecentReviews] 강의 ID {} AI 요약 업데이트 실패", lectureId, e);
            }
        }
    }
}
