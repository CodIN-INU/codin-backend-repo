package inu.codin.codinticketingsse.sse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TicketingSsePerformanceTest {

    @Autowired
    private WebTestClient webTestClient;

    private final int CLIENT_COUNT = 100;
    private final int EVENTS_PER_SECOND = 5;
    private final long TEST_DURATION_SEC = 1;

    @Test
    public void sseThroughputTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(CLIENT_COUNT);
        AtomicBoolean errorFlag = new AtomicBoolean(false);

        // 1. 100명의 클라이언트가 SSE 스트림에 접속
        for (int i = 0; i < CLIENT_COUNT; i++) {
            new Thread(() -> {
                try {
                    Flux<ServerSentEvent<String>> eventFlux = webTestClient.get()
                            .uri("/sse/{eventId}", 1)
                            .accept(MediaType.TEXT_EVENT_STREAM)
                            .exchange()
                            .returnResult(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
                            .getResponseBody();

                    // init ping 포함 총 (EVENTS_PER_SECOND * TEST_DURATION_SEC + 1)개 이벤트 수신 대기
                    eventFlux
                            .take(EVENTS_PER_SECOND * TEST_DURATION_SEC + 1)
                            .collectList()
                            .block(Duration.ofSeconds(TEST_DURATION_SEC + 2));
                } catch (Exception ex) {
                    errorFlag.set(true);
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        // 2. 모든 클라이언트 접속 대기
        Thread.sleep(500);

        // 3. 초당 5회 publishEventStock 스케줄러 실행
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        Runnable publishTask = () -> webTestClient.post()
                .uri(uri -> uri
                        .path("/sse/{eventId}")
                        .queryParam("quantity", 1)
                        .build(1))
                .exchange()
                .expectStatus().is2xxSuccessful();
        scheduler.scheduleAtFixedRate(publishTask, 0, 200, TimeUnit.MILLISECONDS);

        // 4) 테스트 종료까지 대기
        boolean completed = latch.await(TEST_DURATION_SEC + 3, TimeUnit.SECONDS);
        scheduler.shutdownNow();

        Assertions.assertTrue(completed, "모든 클라이언트가 메시지를 수신하지 못했습니다.");
        Assertions.assertFalse(errorFlag.get(), "SSE 전송 중 에러 발생");
    }
}
