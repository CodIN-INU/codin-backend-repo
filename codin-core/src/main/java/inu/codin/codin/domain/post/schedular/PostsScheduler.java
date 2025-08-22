package inu.codin.codin.domain.post.schedular;

import inu.codin.codin.domain.post.schedular.exception.SchedulerErrorCode;
import inu.codin.codin.domain.post.schedular.exception.SchedulerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

/*
    비교과 게시글 매일 새벽 4시 업데이트
 */
@Slf4j
@Component
public class PostsScheduler {

    @Value("${schedule.path}")
    private String PATH;

    @Value("${lecture.python.path}")
    private String PYTHON_DIR;

    @Scheduled(cron = "${schedule.department.cron}", zone = "Asia/Seoul")
    @Async
    public void departmentPostsScheduler() {
        runPythonScript("department.py", "학과 공지사항");
    }

    @Scheduled(cron = "${schedule.starinu.cron}", zone = "Asia/Seoul")
    @Async
    public void starinuPostsScheduler() {
        runPythonScript("starinu.py", "STARINU 공지사항");
    }

    /**
     * Python 스크립트 실행 공통 로직
     *
     * @param fileName 실행할 python 파일명
     * @param taskName 로그 출력용 태스크 이름
     */
    private void runPythonScript(String fileName, String taskName) {
        try {
            ProcessBuilder processBuilder =
                    new ProcessBuilder()
                            .inheritIO()
                            .command(PYTHON_DIR, PATH + fileName);

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            log.warn("Exited {} python with error code {}", taskName, exitCode);
            if (exitCode == 0) {
                log.info("[PostsScheduler] {} 업데이트 완료", taskName);
            } else {
                log.warn("[PostsScheduler] {} 업데이트 실패", taskName);
            }

        } catch (IOException | InterruptedException e) {
            // TODO: InterruptedException 발생 시 현재 스레드 인터럽트 플래그 복구 필요
            //   - Thread.currentThread().interrupt(); 호출 후 적절히 종료 처리
            //   - 단순히 예외 변환만 하면 종료/재시작 시 스레드가 정상적으로 중단되지 않을 수 있음
            log.error("[PostsScheduler] {} 실행 중 오류: {}", taskName, e.getMessage(), e);
            throw new SchedulerException(SchedulerErrorCode.SCHEDULER_INTERRUPT_ERROR);
        }
    }
}
