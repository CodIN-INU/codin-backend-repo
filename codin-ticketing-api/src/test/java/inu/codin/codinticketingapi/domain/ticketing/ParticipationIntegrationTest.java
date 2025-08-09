package inu.codin.codinticketingapi.domain.ticketing;

import inu.codin.codinticketingapi.domain.admin.entity.Event;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.ParticipationResponse;
import inu.codin.codinticketingapi.domain.ticketing.entity.Campus;
import inu.codin.codinticketingapi.domain.ticketing.entity.Department;
import inu.codin.codinticketingapi.domain.ticketing.entity.Participation;
import inu.codin.codinticketingapi.domain.ticketing.entity.Stock;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingErrorCode;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingException;
import inu.codin.codinticketingapi.domain.ticketing.repository.EventRepository;
import inu.codin.codinticketingapi.domain.ticketing.repository.ParticipationRepository;
import inu.codin.codinticketingapi.domain.ticketing.repository.StockRepository;
import inu.codin.codinticketingapi.domain.ticketing.service.ParticipationService;
import inu.codin.codinticketingapi.domain.user.dto.UserInfoResponse;
import inu.codin.codinticketingapi.domain.user.service.UserClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

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

    @MockitoBean
    private UserClientService userClientService;

    private Event testEvent;
    private UserInfoResponse testUser;

    @BeforeEach
    @Transactional
    void setUp() {
        // 테스트 이벤트 생성
        testEvent = Event.builder()
                .userId("userId")
                .title("Test Event")
                .description("Test Description")
                .campus(Campus.SONGDO_CAMPUS)
                .eventTime(LocalDateTime.now().plusDays(1))
                .eventEndTime(LocalDateTime.now().plusDays(2))
                .build();
        testEvent = eventRepository.save(testEvent);
        log.info("테스트 이벤트 id : {}", testEvent.getId());

        // 재고 생성
        Stock stock = Stock.builder()
                .event(testEvent)
                .initialStock(100)
                .build();
        stockRepository.save(stock);

        // 테스트 유저 정보
        testUser = UserInfoResponse.builder()
                .userId("testUser")
                .name("Test User")
                .studentId("202012345")
                .department(Department.COMPUTER_SCI)
                .build();

        given(userClientService.fetchUser()).willReturn(testUser);
    }

//    @Test
//    @DisplayName("티켓 번호 순차 증가 검증")
//    void 티켓번호_순차증가_검증() throws InterruptedException {
//        participationRepository.deleteAll();
//
//        // 초기 재고를 적게 설정하여 경쟁 상태 유발
//        Stock stock = stockRepository.findByEvent(testEvent).orElseThrow();
//        stock.updateStock(10); // 10개로 제한
//        stock = stockRepository.save(stock);
//
//        log.info("stock : {}", stock.getStock());
//
//        int threadCount = 15; // 재고보다 많은 스레드
//        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
//        CountDownLatch latch = new CountDownLatch(threadCount);
//        AtomicInteger successCount = new AtomicInteger(0);
//        AtomicInteger failCount = new AtomicInteger(0);
//
//        for (int i = 0; i < threadCount; i++) {
//            final int userIndex = i;
//            executorService.execute(() -> {
//                try {
//                    // 각 스레드마다 고유한 사용자 생성
//                    UserInfoResponse user = UserInfoResponse.builder()
//                            .userId("user" + userIndex)
//                            .name("User " + userIndex)
//                            .studentId("20201234" + userIndex)
//                            .department(Department.COMPUTER_SCI)
//                            .build();
//
//                    // 스레드 로컬 목킹
//                    when(userClientService.fetchUser()).thenReturn(user);
//
//                    ParticipationResponse response = participationService.saveParticipation(testEvent.getId());
//                    successCount.incrementAndGet();
//                    log.info("사용자 {} 티켓 번호: {}", user.getUserId(), response.getTicketNumber());
//
//                } catch (TicketingException e) {
//                    if (e.getErrorCode() == TicketingErrorCode.SOLD_OUT) {
//                        failCount.incrementAndGet();
//                        log.info("재고 부족으로 실패");
//                    } else {
//                        log.error("예상치 못한 오류: {}", e.getMessage());
//                    }
//                } catch (Exception e) {
//                    log.error("처리 중 오류 발생: {}", e.getMessage());
//                } finally {
//                    latch.countDown();
//                }
//            });
//        }
//
//        latch.await();
//        executorService.shutdown();
//
//        log.info("성공 회수 : {}", successCount);
//        log.info("실패 회수 : {}", failCount);
//        // 검증
//        assertThat(successCount.get()).isEqualTo(10); // 재고만큼만 성공
//        assertThat(failCount.get()).isEqualTo(5);     // 나머지는 재고 부족으로 실패
//
//        // 티켓 번호 중복 검사
//        List<Participation> participations = participationRepository.findAllByEvent_Id(testEvent.getId());
//        Set<Integer> ticketNumbers = participations.stream()
//                .map(Participation::getTicketNumber)
//                .collect(Collectors.toSet());
//
//        assertThat(ticketNumbers).hasSize(10); // 모든 티켓 번호가 고유해야 함
//        assertThat(ticketNumbers).containsExactlyInAnyOrder(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
//    }

    @Test
    @DisplayName("재고 부족 시 예외 발생")
    @Transactional
    void 재고부족시_예외발생() {
        Event event = Event.builder()
                .title("Test Event")
                .userId("userId")
                .description("Test Description")
                .campus(Campus.SONGDO_CAMPUS)
                .eventTime(LocalDateTime.now().plusDays(1))
                .eventEndTime(LocalDateTime.now().plusDays(2))
                .build();
        Event savedEvent = eventRepository.save(event);

        Stock stock = Stock.builder()
                .event(savedEvent)
                .initialStock(0)
                .build();
        stock = stockRepository.save(stock);
        stock.decrease();
        stockRepository.save(stock);

        // 참여 시도 시 예외 발생 확인
        assertThatThrownBy(() -> participationService.saveParticipation(savedEvent.getId()))
                .isInstanceOf(TicketingException.class)
                .hasFieldOrPropertyWithValue("errorCode", TicketingErrorCode.SOLD_OUT);
    }

    @Test
    @DisplayName("중복 참여 시 기존 참여 내용 반환")
    @Transactional
    void 중복_참여_시_기존_참여_내용_반환() {
        // 첫 번째 참여
        ParticipationResponse firstResponse = participationService.saveParticipation(testEvent.getId());

        // 같은 사용자가 다시 참여 시도
        ParticipationResponse secondResponse = participationService.saveParticipation(testEvent.getId());

        // 같은 참여 내용이 반환되는지 확인
        assertThat(firstResponse.getTicketNumber()).isEqualTo(secondResponse.getTicketNumber());
    }
}