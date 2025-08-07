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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
public class ParticipationIntegrationTest {

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
    private Stock testStock;
    private UserInfoResponse testUser;

    @BeforeEach
    void setUp() {
        // 테스트 이벤트 생성
        testEvent = Event.builder()
                .userId("adminUser")
                .campus(Campus.SONGDO_CAMPUS)
                .eventTime(LocalDateTime.now().plusDays(1))
                .eventEndTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .title("통합 테스트 이벤트")
                .locationInfo("정보대 학생회실")
                .target("정보대 학생")
                .description("통합 테스트용 이벤트입니다")
                .inquiryNumber("010-1234-5678")
                .promotionLink("https://example.com")
                .build();

        // 재고 생성
        testStock = Stock.builder()
                .event(testEvent)
                .initialStock(100)
                .build();

        testEvent.setStock(testStock);
        testEvent = eventRepository.save(testEvent);

        // 테스트 사용자 정보
        testUser = UserInfoResponse.builder()
                .userId("testUser123")
                .email("test@inu.ac.kr")
                .name("테스트사용자")
                .studentId("202012345")
                .department(Department.COMPUTER_SCI)
                .build();

        // 스레드별 별도 모킹 사용자 정보 제공
        when(userClientService.fetchUser()).thenAnswer(invocation -> {
            String threadName = Thread.currentThread().getName();
            return UserInfoResponse.builder()
                    .userId("user_" + threadName)
                    .name("테스트사용자_" + threadName)
                    .studentId("202012345")
                    .department(Department.COMPUTER_SCI)
                    .build();
        });
    }

    @Test
    @DisplayName("정상적인 참여 저장 통합 테스트")
    void saveParticipation_정상케이스() {
        // given
        given(userClientService.fetchUser()).willReturn(testUser);

        // when
        ParticipationResponse response = participationService.saveParticipation(testEvent.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTicketNumber()).isEqualTo(1);

        // 데이터베이스 검증
        Participation savedParticipation = participationRepository.findByEventAndUserId(testEvent, testUser.getUserId())
                .orElseThrow();

        assertThat(savedParticipation.getName()).isEqualTo(testUser.getName());
        assertThat(savedParticipation.getStudentId()).isEqualTo(testUser.getStudentId());
        assertThat(savedParticipation.getDepartment()).isEqualTo(testUser.getDepartment());
        assertThat(savedParticipation.getTicketNumber()).isEqualTo(1);

        // 재고 감소 검증
        Stock updatedStock = stockRepository.findByEvent(testEvent).orElseThrow();
        assertThat(updatedStock.getStock()).isEqualTo(99);
    }

    @Test
    @DisplayName("존재하지 않는 이벤트로 참여 시도")
    void saveParticipation_이벤트없음() {
        // given
        given(userClientService.fetchUser()).willReturn(testUser);
        Long nonExistentEventId = 999L;

        // when & then
        assertThatThrownBy(() -> participationService.saveParticipation(nonExistentEventId))
                .isInstanceOf(TicketingException.class)
                .hasFieldOrPropertyWithValue("errorCode", TicketingErrorCode.EVENT_NOT_FOUND);
    }

    @Test
    @DisplayName("재고가 소진된 이벤트 참여 시도")
    void saveParticipation_재고소진() {
        // given
        // 재고를 0으로 설정
        testStock.updateStock(0);
        stockRepository.save(testStock);

        given(userClientService.fetchUser()).willReturn(testUser);

        // when & then
        assertThatThrownBy(() -> participationService.saveParticipation(testEvent.getId()))
                .isInstanceOf(TicketingException.class)
                .hasFieldOrPropertyWithValue("errorCode", TicketingErrorCode.SOLD_OUT);
    }

    @Test
    @DisplayName("동시성 테스트 - 여러 사용자가 동시에 참여")
    void saveParticipation_동시성테스트() throws InterruptedException {
        // given
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // 재고를 5개로 제한
        testStock.updateStock(5);
        stockRepository.save(testStock);

        // when
        for (int i = 0; i < threadCount; i++) {
            final int userIndex = i;
            executorService.submit(() -> {
                try {
                    UserInfoResponse user = UserInfoResponse.builder()
                            .userId("testUser" + userIndex)
                            .email("test" + userIndex + "@inu.ac.kr")
                            .name("테스트사용자" + userIndex)
                            .studentId("20201234" + userIndex)
                            .department(Department.COMPUTER_SCI)
                            .build();

                    given(userClientService.fetchUser()).willReturn(user);
                    participationService.saveParticipation(testEvent.getId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(5); // 재고만큼만 성공
        assertThat(failCount.get()).isEqualTo(5);    // 나머지는 실패

        // 최종 재고 확인
        Stock finalStock = stockRepository.findByEvent(testEvent).orElseThrow();
        assertThat(finalStock.getStock()).isEqualTo(0);
    }

    @Test
    @DisplayName("티켓 번호 순차 증가 검증")
    void saveParticipation_티켓번호순차증가() {
        // given
        testStock.updateStock(98); // 초기 재고 100에서 2개 소진된 상태
        stockRepository.save(testStock);

        given(userClientService.fetchUser()).willReturn(testUser);

        // when
        ParticipationResponse response = participationService.saveParticipation(testEvent.getId());

        // then
        // 100 - 98 + 1 = 3번째 티켓
        assertThat(response.getTicketNumber()).isEqualTo(3);

        // 재고 확인
        Stock updatedStock = stockRepository.findByEvent(testEvent).orElseThrow();
        assertThat(updatedStock.getStock()).isEqualTo(97);
    }
}