package inu.codin.codinticketingapi.domain.admin.scheduler;

import inu.codin.codinticketingapi.domain.admin.entity.Event;
import inu.codin.codinticketingapi.domain.admin.entity.EventStatus;
import inu.codin.codinticketingapi.domain.ticketing.repository.EventRepository;
import inu.codin.codinticketingapi.domain.ticketing.scheduler.StockCheckJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventStatusScheduler implements ApplicationRunner {

    private final Scheduler scheduler;
    private final EventRepository eventRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        scheduleUpcomingEvents();
        scheduleActiveEvents();
    }

    public void scheduleCreateOrUpdatedEvent(Event event) {
        try {
            scheduler.deleteJob(new JobKey("startJob-" + event.getId(), "event-status"));
            scheduler.deleteJob(new JobKey("endJob-" + event.getId(), "event-status"));

            if (event.getEventStatus() == EventStatus.UPCOMING && event.getEventTime().isAfter(LocalDateTime.now())) {
                JobDetail startJobDetail = createStartJob(event);
                Trigger startTrigger = createStartTrigger(event);
                scheduler.scheduleJob(startJobDetail, startTrigger);
                log.info("새/업데이트 이벤트 시작 Job 스케줄링: ID = {}", event.getId());

//                JobDetail endJobDetail = createEndJob(event);
//                Trigger endTrigger = createEndTrigger(event);
//                scheduler.scheduleJob(endJobDetail, endTrigger);
//                log.info("새/업데이트 이벤트 종료 Job 스케줄링: ID = {}", event.getId());

                JobDetail stockJob = createStockCheckJob(event);
                Trigger stockTrigger = createStockCheckTrigger(event);
                scheduler.scheduleJob(stockJob, stockTrigger);
                log.info("새/업데이트 이벤트 재고 상태 Job 스케줄링: ID = {}", event.getId());
            } else {
                log.info("이벤트 ID {}는 UPCOMING 상태가 아니거나 이미 시작 시간이 지났으므로 스케줄링하지 않습니다.", event.getId());
            }
        } catch (SchedulerException e) {
            log.error("이벤트 스케줄 삭제 중 오류 발생: Event ID = {}", event.getId(), e);
        }
    }

    private void scheduleUpcomingEvents() {
        List<Event> upcomingEvents = eventRepository.findByEventStatusAndEventTimeAfterAndDeletedAtIsNull(EventStatus.UPCOMING, LocalDateTime.now());

        if (upcomingEvents.isEmpty()) {
            log.info("스케줄링할 UPCOMING 이벤트가 없습니다.");

            return;
        }

        for (Event event : upcomingEvents) {
            try {
                JobDetail startJobDetail = createStartJob(event);
                Trigger startTrigger = createStartTrigger(event);

                scheduler.scheduleJob(startJobDetail, startTrigger);
                log.info("이벤트 시작 Job 스케줄링 완료: Event ID = {}, 시작 시간 = {}", event.getId(), event.getEventTime());

//                JobDetail endJobDetail = createEndJob(event);
//                Trigger endTrigger = createEndTrigger(event);
//
//                scheduler.scheduleJob(endJobDetail, endTrigger);
//                log.info("이벤트 종료 Job 스케줄링 완료: Event ID = {}, 종료 시간 = {}", event.getId(), event.getEventEndTime());

            } catch (SchedulerException e) {
                log.error("이벤트 스케줄링 중 오류 발생: Event ID = {}", event.getId(), e);
            }
        }

        log.info("모든 UPCOMING 이벤트 스케줄링 완료.");
    }

    /**
     * 이미 ACTIVE 상태인 이벤트들의 StockCheckJob 스케줄링
     */
    private void scheduleActiveEvents() {
        List<Event> activeEvents = eventRepository.findByEventStatusAndEventEndTimeAfterAndDeletedAtIsNull(EventStatus.ACTIVE, LocalDateTime.now());

        if (activeEvents.isEmpty()) {
            log.info("스케줄링할 ACTIVE 이벤트가 없습니다.");
            return;
        }

        for (Event event : activeEvents) {
            try {
//                // 종료 Job만 스케줄링 (시작은 이미 지났으므로)
//                JobDetail endJobDetail = createEndJob(event);
//                Trigger endTrigger = createEndTrigger(event);
//                scheduler.scheduleJob(endJobDetail, endTrigger);
//                log.info("ACTIVE 이벤트 종료 Job 스케줄링: Event ID = {}", event.getId());

                // StockCheckJob은 즉시 시작
                JobDetail stockJob = createStockCheckJob(event);
                Trigger stockTrigger = createActiveStockCheckTrigger(event);
                scheduler.scheduleJob(stockJob, stockTrigger);
                log.info("ACTIVE 이벤트 재고 Job 스케줄링: Event ID = {}", event.getId());

            } catch (SchedulerException e) {
                log.error("ACTIVE 이벤트 스케줄링 중 오류: Event ID = {}", event.getId(), e);
            }
        }

        log.info("모든 ACTIVE 이벤트 스케줄링 완료.");
    }

    /**
     * ACTIVE 이벤트용 StockCheck 트리거 (즉시 시작)
     */
    private Trigger createActiveStockCheckTrigger(Event event) {
        return newTrigger()
                .withIdentity("stockTrigger-" + event.getId(), "stock")
                .startNow() // 즉시 시작
                .endAt(Date.from(event.getEventEndTime().atZone(ZoneId.systemDefault()).toInstant()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(1)
                        .repeatForever()
                        .withMisfireHandlingInstructionFireNow())
                .build();
    }

    public void deleteOpenEventScheduler(Long eventId) {
        try {
            scheduler.deleteJob(new JobKey("startJob-" + eventId, "event-status"));

        } catch (SchedulerException e) {
            log.error("이벤트 시작 Job 삭제 중 오류 발생: Event ID = {}", eventId, e);
        }
    }

    public void scheduleAllDelete(Event event) {
        try {
            scheduler.deleteJob(new JobKey("startJob-" + event.getId(), "event-status"));
            scheduler.deleteJob(new JobKey("endJob-" + event.getId(), "event-status"));
            event.closeEvent();
        } catch (SchedulerException e) {
            log.error("새/업데이트 이벤트 스케줄링 중 오류 발생: Event ID = {}", event.getId(), e);
        }
    }

    private JobDetail createStartJob(Event event) {
        return newJob(EventStartJob.class)
                .withIdentity("startJob-" + event.getId(), "event-status")
                .usingJobData("eventId", event.getId())
                .build();
    }

    private Trigger createStartTrigger(Event event) {
        return newTrigger()
                .withIdentity("startTrigger-" + event.getId(), "event-status")
                .startAt(Date.from(event.getEventTime().atZone(ZoneId.systemDefault()).toInstant()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }

    // 이벤트 종료를 수동으로 조작
    private JobDetail createEndJob(Event event) {
        return newJob(EventEndJob.class)
                .withIdentity("endJob-" + event.getId(), "event-status")
                .usingJobData("eventId", event.getId())
                .build();
    }

    // 이벤트 종료를 수동으로 조작
    private Trigger createEndTrigger(Event event) {
        return newTrigger()
                .withIdentity("endTrigger-" + event.getId(), "event-status")
                .startAt(Date.from(event.getEventEndTime().atZone(ZoneId.systemDefault()).toInstant()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }

    private JobDetail createStockCheckJob(Event event) {
        return newJob(StockCheckJob.class)
                .withIdentity("stockJob-" + event.getId(), "stock")
                .usingJobData("eventId", event.getId())
                .build();
    }

    /**
     * 이벤트 시작 시간부터 이벤트 종료시각까지 1초에 한번씩 Job 트리거
     * @param event 이벤트
     * @return Trigger
     */
    private Trigger createStockCheckTrigger(Event event) {
        return newTrigger()
                .withIdentity("stockTrigger-" + event.getId(), "stock")
                .startAt(Date.from(event.getEventTime().atZone(ZoneId.systemDefault()).toInstant()))
                .endAt(Date.from(event.getEventEndTime().atZone(ZoneId.systemDefault()).toInstant()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(1)
                        .repeatForever()
                        .withMisfireHandlingInstructionFireNow())
                .build();
    }
}