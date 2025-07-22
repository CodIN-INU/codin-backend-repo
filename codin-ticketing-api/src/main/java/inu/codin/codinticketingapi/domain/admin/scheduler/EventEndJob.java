package inu.codin.codinticketingapi.domain.admin.scheduler;

import inu.codin.codinticketingapi.domain.admin.entity.EventStatus;
import inu.codin.codinticketingapi.domain.ticketing.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventEndJob implements Job {
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Long eventId = context.getJobDetail().getJobDataMap().getLong("eventId");
        log.info("EventEndJob 실행: Event ID = {}", eventId);

        eventRepository.findById(eventId).ifPresent(event -> {
            if (event.getEventStatus() == EventStatus.ACTIVE) {
                event.updateStatus(EventStatus.ENDED);
                log.info("이벤트 종료 Event ID = {}, 현재 상태: {}", eventId, event.getEventStatus());

                try {
                    Scheduler scheduler = context.getScheduler();
                    scheduler.deleteJob(context.getJobDetail().getKey());
                    log.info("EventEndJob 삭제 완료: Event ID = {}", eventId);
                } catch (SchedulerException e) {
                    log.error("EventEndJob 삭제 중 오류 발생: Event ID = {}", eventId, e);
                }
            } else {
                log.warn("이벤트 종료 Job 실행되었으나, 이벤트 상태가 ACTIVE가 아님. Event ID = {}, 현재 상태: {}", eventId, event.getEventStatus());
            }
        });
    }
}
