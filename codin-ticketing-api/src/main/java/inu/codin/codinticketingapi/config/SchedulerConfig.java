package inu.codin.codinticketingapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
public class SchedulerConfig implements SchedulingConfigurer {
    private static final int POOL_SIZE = 10;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        // 스케줄러가 사용할 스레드 풀의 개수를 설정
        scheduler.setPoolSize(POOL_SIZE);

        // 스레드 이름 접두사 설정
        scheduler.setThreadNamePrefix("scheduled-task-");

        // 스케줄러 초기화
        scheduler.initialize();

        // 생성한 스케줄러를 등록
        taskRegistrar.setTaskScheduler(scheduler);
    }
}
