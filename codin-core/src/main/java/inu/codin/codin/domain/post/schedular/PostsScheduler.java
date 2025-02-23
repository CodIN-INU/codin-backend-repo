package inu.codin.codin.domain.post.schedular;

import inu.codin.codin.domain.post.exception.SchedulerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

/*
    비교과 게시글 매일 새벽 2시 업데이트
 */
@Slf4j
@Component
public class PostsScheduler {

    private static final String PATH = "/home/codin-ubuntu/codin-main/backend-data/post_data/";

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Async
    public void departmentPostsScheduler() {
        String fileName = "department.py";
        ProcessBuilder processBuilder = new ProcessBuilder().inheritIO().command("/usr/bin/python3",
                PATH+fileName);
        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            log.warn("Exited department python with error code" + exitCode);
            log.info("[PostsScheduler] 학과 공지사항 업데이트 완료");
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e.getStackTrace()[0]);
            throw new SchedulerException(e.getMessage());
        }
    }

    @Scheduled(cron = "0 30 0 * * *", zone = "Asia/Seoul")
    @Async
    public void starinuPostsScheduler(){
        String fileName = "starinu.py";
        ProcessBuilder processBuilder = new ProcessBuilder().inheritIO().command("/usr/bin/python3",
                PATH+fileName);
        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            log.warn("Exited starinu python with error code" + exitCode);
            log.info("[PostsScheduler] STARINU 게시글 업데이트 완료");
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e.getStackTrace()[0]);
            throw new SchedulerException(e.getMessage());
        }
    }
}
