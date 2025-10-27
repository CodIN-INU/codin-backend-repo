package inu.codin.codinticketingsse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    // todo: 서버 상태에 맞게 조절

    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 코어(최소) 스레드 수
        executor.setCorePoolSize(4);
        // 최대 스레드 수
        executor.setMaxPoolSize(10);
        // 최대 대기 큐 크기
        executor.setQueueCapacity(100);
        // 유휴 스레드(코어 초과) 유지 시간(초)
        executor.setKeepAliveSeconds(10);
        // 스레드 이름 접두사
        executor.setThreadNamePrefix("MyExec-");
        // JVM 종료 대기 시간(초) — shutdown 시 대기 여부
        executor.setAwaitTerminationSeconds(30);
        executor.setWaitForTasksToCompleteOnShutdown(true);

        executor.initialize();
        return executor;
    }
}
