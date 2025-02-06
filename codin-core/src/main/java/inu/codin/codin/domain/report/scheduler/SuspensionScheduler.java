package inu.codin.codin.domain.report.scheduler;

import inu.codin.codin.domain.report.service.SuspensionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SuspensionScheduler {

    private final SuspensionService suspensionService;

    @Scheduled(cron = "0 0 * * * ?") // 매 정시마다 실행
    //@Scheduled(cron = "0 * * * * ?") // 매 1분마다 실행(테스트)
    public void checkAndReleaseSuspendedUsers() {
        log.info("정지 해제 스케줄러 실행...");
        suspensionService.releaseSuspendedUsers();
    }
}
