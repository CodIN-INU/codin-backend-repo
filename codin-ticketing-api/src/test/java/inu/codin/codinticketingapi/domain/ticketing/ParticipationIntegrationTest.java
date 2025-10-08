package inu.codin.codinticketingapi.domain.ticketing;

import inu.codin.codinticketingapi.domain.admin.entity.Event;
import inu.codin.codinticketingapi.domain.admin.entity.EventStatus;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.ParticipationResponse;
import inu.codin.codinticketingapi.domain.ticketing.entity.Campus;
import inu.codin.codinticketingapi.domain.ticketing.entity.Department;
import inu.codin.codinticketingapi.domain.ticketing.entity.Participation;
import inu.codin.codinticketingapi.domain.ticketing.entity.Stock;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingErrorCode;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingException;
import inu.codin.codinticketingapi.domain.ticketing.redis.RedisEventService;
import inu.codin.codinticketingapi.domain.ticketing.repository.EventRepository;
import inu.codin.codinticketingapi.domain.ticketing.repository.ParticipationRepository;
import inu.codin.codinticketingapi.domain.ticketing.repository.StockRepository;
import inu.codin.codinticketingapi.domain.ticketing.service.ParticipationService;
import inu.codin.codinticketingapi.domain.user.dto.UserInfoResponse;
import inu.codin.codinticketingapi.domain.user.service.UserClientService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ActiveProfiles("test")
@SpringBootTest
public class ParticipationIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(ParticipationIntegrationTest.class);
    @Autowired
    private ParticipationService participationService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ParticipationRepository participationRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private RedisEventService redisEventService;

    @Autowired
    private RedisTemplate<String, ParticipationResponse> participationRedisTemplate;

    @MockitoBean
    private UserClientService userClientService;

    private Event testEvent;
    private Event savedTestEvent;
    private UserInfoResponse testUser;

    @BeforeEach
    void setUp() {
        // 테스트 이벤트 생성
        testEvent = Event.builder()
                .userId("userId")
                .title("Test Event")
                .description("Test Description")
                .campus(Campus.SONGDO_CAMPUS)
                .eventTime(LocalDateTime.now().minusDays(1))
                .eventEndTime(LocalDateTime.now().plusDays(2))
                .build();
        testEvent.updateStatus(EventStatus.ACTIVE);

        // 재고 생성
        Stock stock = Stock.builder()
                .event(testEvent)
                .initialStock(10)
                .build();

        savedTestEvent = eventRepository.saveAndFlush(testEvent);
        redisEventService.initializeTickets(savedTestEvent.getId(), savedTestEvent.getStock().getCurrentTotalStock());

        // 테스트 유저 정보
        testUser = UserInfoResponse.builder()
                .userId("testUser")
                .name("Test User")
                .studentId("202012345")
                .department(Department.COMPUTER_SCI)
                .build();

        redisEventService.initializeTickets(savedTestEvent.getId(), savedTestEvent.getStock().getCurrentTotalStock());

        given(userClientService.fetchUser()).willReturn(testUser);
    }

    @AfterEach
    void tearDown() {
        participationRepository.deleteAllInBatch();
        stockRepository.deleteAllInBatch();
        eventRepository.deleteAllInBatch();

        participationRedisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }

    @Test
    @DisplayName("티켓 번호 순차 증가 검증")
    void 티켓번호_순차증가_검증() throws ExecutionException, InterruptedException {
        // given
        int threadCount = 15; // 재고보다 많은 스레드
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        CyclicBarrier cyclicBarrier = new CyclicBarrier(threadCount);
        List<CompletableFuture<Void>> tasks = new ArrayList<>();
        AtomicInteger userIndexCounter = new AtomicInteger(0);

        given(userClientService.fetchUser()).willAnswer(invocation -> {
            int userIndex = userIndexCounter.getAndIncrement();

            return UserInfoResponse.builder()
                    .userId("user" + userIndex)
                    .name("User " + userIndex)
                    .studentId("20201234" + userIndex)
                    .department(Department.COMPUTER_SCI)
                    .build();
        });

        // when
        for (int i = 1; i <= threadCount; i++) {
            tasks.add(CompletableFuture.runAsync(() -> {
                try {

                    cyclicBarrier.await();
                    ParticipationResponse response = participationService.saveParticipation(savedTestEvent.getId());
                    successCount.incrementAndGet();
                } catch (TicketingException e) {
                    if (e.getErrorCode() == TicketingErrorCode.SOLD_OUT) {
                        failCount.incrementAndGet();
                        log.info("재고 부족으로 실패");
                    } else {
                        log.error("예상치 못한 오류: {}", e.getMessage());
                    }
                } catch (Exception e) {
                    log.error("처리 중 오류 발생: {}", e.getMessage());
                }
            }, executorService));
        }

        executorService.shutdown();
        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).get();

        log.info("성공 회수 : {}", successCount);
        log.info("실패 회수 : {}", failCount);

        // 티켓 번호 중복 검사
        List<Participation> participationList = participationRepository.findAllByEvent_Id(testEvent.getId());
        Set<Integer> ticketNumbers = participationList.stream()
                .map(Participation::getTicketNumber)
                .collect(Collectors.toSet());

        // then
        assertThat(successCount.get()).isEqualTo(10); // 재고만큼만 성공
        assertThat(failCount.get()).isEqualTo(5);     // 나머지는 재고 부족으로 실패
        assertThat(ticketNumbers).hasSize(10); // 모든 티켓 번호가 고유해야 함
        assertThat(ticketNumbers).containsExactlyInAnyOrder(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    @DisplayName("재고 부족 시 예외 발생")
    @Transactional
    void 재고부족시_예외발생() {
        // given
        Event event = Event.builder()
                .title("Test Event")
                .userId("userId")
                .description("Test Description")
                .campus(Campus.SONGDO_CAMPUS)
                .eventTime(LocalDateTime.now().minusDays(1))
                .eventEndTime(LocalDateTime.now().plusDays(2))
                .build();
        event.updateStatus(EventStatus.ACTIVE);
        Event savedEvent = eventRepository.save(event);

        Stock stock = Stock.builder()
                .event(savedEvent)
                .initialStock(0)
                .build();
        stock = stockRepository.save(stock);
        stock.decrease();
        stockRepository.save(stock);

        // when & then
        // 참여 시도 시 예외 발생 확인
        assertThatThrownBy(() -> participationService.saveParticipation(savedEvent.getId()))
                .isInstanceOf(TicketingException.class)
                .hasFieldOrPropertyWithValue("errorCode", TicketingErrorCode.SOLD_OUT);
    }

    @Test
    @DisplayName("중복 참여 시 기존 참여 내용 반환")
    @Transactional
    void 중복_참여_시_기존_참여_내용_반환() {
        // given & when
        // 첫 번째 참여
        ParticipationResponse firstResponse = participationService.saveParticipation(testEvent.getId());
        // 같은 사용자가 다시 참여 시도
        ParticipationResponse secondResponse = participationService.saveParticipation(testEvent.getId());

        // then
        // 같은 참여 내용이 반환되는지 확인
        assertThat(firstResponse.getTicketNumber()).isEqualTo(secondResponse.getTicketNumber());
    }
}