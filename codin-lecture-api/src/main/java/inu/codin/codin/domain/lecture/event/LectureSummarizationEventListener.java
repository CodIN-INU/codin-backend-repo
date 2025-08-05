package inu.codin.codin.domain.lecture.event;

import inu.codin.codin.domain.lecture.service.LectureSummarizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class LectureSummarizationEventListener {

    private final LectureSummarizationService summarizationService;

    // todo: 현재 리뷰 작성 시에서만 작동, Lecture 엔티티에 생성 필요.

    @Async("aiSummaryExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLectureChange(LectureSummarizationEvent event) {
        try {
            log.info("[onLectureChange] Lecture AI 요약 이벤트 호출 lectureId: {}", event.lectureId());
            summarizationService.summarizeLecture(event.lectureId());
            log.info("[onLectureChange] Lecture AI 요약 완료 lectureId: {}", event.lectureId());
        } catch (Exception e) {
            log.error("[onLectureChange] AI 요약 실패 lectureId: {}", event.lectureId(), e);
        }
    }
}