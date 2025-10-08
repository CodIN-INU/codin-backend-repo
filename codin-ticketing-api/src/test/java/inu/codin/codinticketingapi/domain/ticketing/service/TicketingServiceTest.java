package inu.codin.codinticketingapi.domain.ticketing.service;

import inu.codin.codinticketingapi.domain.admin.entity.Event;
import inu.codin.codinticketingapi.domain.admin.entity.EventStatus;
import inu.codin.codinticketingapi.domain.image.service.ImageService;
import inu.codin.codinticketingapi.domain.ticketing.dto.event.ParticipationStatusChangedEvent;
import inu.codin.codinticketingapi.domain.ticketing.entity.Campus;
import inu.codin.codinticketingapi.domain.ticketing.entity.Participation;
import inu.codin.codinticketingapi.domain.ticketing.entity.ParticipationStatus;
import inu.codin.codinticketingapi.domain.ticketing.entity.Stock;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingErrorCode;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingException;
import inu.codin.codinticketingapi.domain.ticketing.repository.EventRepository;
import inu.codin.codinticketingapi.domain.ticketing.repository.ParticipationRepository;
import inu.codin.codinticketingapi.domain.ticketing.repository.StockRepository;
import inu.codin.codinticketingapi.domain.user.dto.UserInfoResponse;
import inu.codin.codinticketingapi.domain.user.service.UserClientService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketingServiceTest {

    @InjectMocks
    private TicketingService ticketingService;

    @Mock
    private EventRepository eventRepository;
    @Mock
    private ParticipationRepository participationRepository;
    @Mock
    private StockRepository stockRepository;

    @Mock
    private UserClientService userClientService;
    @Mock
    private ImageService imageService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private MultipartFile signatureImage;

    // 테스트 상수들
    private static final Long TEST_EVENT_ID = 1L;
    private static final Long NON_EXISTENT_EVENT_ID = 999L;
    private static final String TEST_USER_ID = "testUser";
    private static final String TEST_USER_NAME = "TEST_USER";
    private static final String TEST_STUDENT_ID = "202012345";
    private static final String ADMIN_USER_ID = "adminUser";
    private static final String TEST_EVENT_TITLE = "TEST_EVENT";
    private static final String TEST_EVENT_DESCRIPTION = "TEST_EVENT_DESCRIPTION";
    private static final String TEST_LOCATION = "TEST_LOCATION";
    private static final String TEST_TARGET = "TEST_TARGET";
    private static final String CORRECT_PASSWORD = "1234";
    private static final String WRONG_PASSWORD = "5678";
    private static final String TEST_SIGNATURE_URL = "http://test-signature.com/image.jpg";
    private static final int INITIAL_STOCK = 100;
    private static final int CURRENT_STOCK_50 = 50;
    private static final int ZERO_STOCK = 0;

    @Test
    @DisplayName("재고 감소 - 정상")
    void decrement_성공() {
        // given
        Event mockEvent = createMockEvent(TEST_EVENT_ID, TEST_EVENT_TITLE, EventStatus.ACTIVE);
        Stock mockStock = Stock.builder()
                .event(mockEvent)
                .initialStock(INITIAL_STOCK)
                .build();
        mockStock.updateStock(CURRENT_STOCK_50);

        given(stockRepository.findByEvent_Id(TEST_EVENT_ID)).willReturn(Optional.of(mockStock));

        // when
        Stock result = ticketingService.decrement(TEST_EVENT_ID);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRemainingStock()).isEqualTo(CURRENT_STOCK_50 - 1);
        verify(stockRepository).findByEvent_Id(TEST_EVENT_ID);
    }

    @Test
    @DisplayName("재고 감소 - 재고 없음")
    void decrement_재고없음() {
        // given
        given(stockRepository.findByEvent_Id(NON_EXISTENT_EVENT_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> ticketingService.decrement(NON_EXISTENT_EVENT_ID))
                .isInstanceOf(TicketingException.class)
                .hasFieldOrPropertyWithValue("errorCode", TicketingErrorCode.STOCK_NOT_FOUND);

        verify(stockRepository).findByEvent_Id(NON_EXISTENT_EVENT_ID);
    }

    @Test
    @DisplayName("재고 감소 - 품절")
    void decrement_품절() {
        // given
        Event mockEvent = createMockEvent(TEST_EVENT_ID, TEST_EVENT_TITLE, EventStatus.ACTIVE);
        Stock mockStock = Stock.builder()
                .event(mockEvent)
                .initialStock(INITIAL_STOCK)
                .build();
        mockStock.updateStock(ZERO_STOCK);

        given(stockRepository.findByEvent_Id(TEST_EVENT_ID)).willReturn(Optional.of(mockStock));

        // when & then
        assertThatThrownBy(() -> ticketingService.decrement(TEST_EVENT_ID))
                .isInstanceOf(TicketingException.class)
                .hasFieldOrPropertyWithValue("errorCode", TicketingErrorCode.SOLD_OUT);

        verify(stockRepository).findByEvent_Id(TEST_EVENT_ID);
    }

    @Test
    @DisplayName("티켓팅 유저 수령 처리 - 정상")
    void processParticipationSuccess_성공() {
        // given
        UserInfoResponse userInfo = createUserInfo(TEST_USER_ID, TEST_USER_NAME);
        Event mockEvent = createMockEvent(TEST_EVENT_ID, TEST_EVENT_TITLE, EventStatus.ACTIVE);
        mockEvent = spy(mockEvent);
        given(mockEvent.getEventPassword()).willReturn(CORRECT_PASSWORD);

        Participation mockParticipation = createMockParticipation(mockEvent, userInfo, ParticipationStatus.WAITING);

        given(userClientService.fetchUser()).willReturn(userInfo);
        given(eventRepository.findById(TEST_EVENT_ID)).willReturn(Optional.of(mockEvent));
        given(imageService.handleImageUpload(signatureImage)).willReturn(TEST_SIGNATURE_URL);
        given(participationRepository.findByEventAndUserId(mockEvent, TEST_USER_ID)).willReturn(Optional.of(mockParticipation));

        // when
        ticketingService.processParticipationSuccess(TEST_EVENT_ID, CORRECT_PASSWORD, signatureImage);

        // then
        verify(userClientService).fetchUser();
        verify(eventRepository).findById(TEST_EVENT_ID);
        verify(imageService).handleImageUpload(signatureImage);
        verify(participationRepository).findByEventAndUserId(mockEvent, TEST_USER_ID);
        verify(mockParticipation).changeStatusCompleted(TEST_SIGNATURE_URL);
    }

    @Test
    @DisplayName("티켓팅 유저 수령 처리 - 이벤트 없음")
    void processParticipationSuccess_이벤트없음() {
        // given
        UserInfoResponse userInfo = createUserInfo(TEST_USER_ID, TEST_USER_NAME);
        given(userClientService.fetchUser()).willReturn(userInfo);
        given(eventRepository.findById(NON_EXISTENT_EVENT_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> ticketingService.processParticipationSuccess(NON_EXISTENT_EVENT_ID, CORRECT_PASSWORD, signatureImage))
                .isInstanceOf(TicketingException.class)
                .hasFieldOrPropertyWithValue("errorCode", TicketingErrorCode.EVENT_NOT_FOUND);

        verify(userClientService).fetchUser();
        verify(eventRepository).findById(NON_EXISTENT_EVENT_ID);
        verify(imageService, never()).handleImageUpload(any(MultipartFile.class));
        verify(participationRepository, never()).findByEventAndUserId(any(), any());
    }

    @Test
    @DisplayName("티켓팅 유저 수령 처리 - 이벤트 비활성화")
    void processParticipationSuccess_이벤트비활성화() {
        // given
        UserInfoResponse userInfo = createUserInfo(TEST_USER_ID, TEST_USER_NAME);
        Event mockEvent = createMockEvent(TEST_EVENT_ID, TEST_EVENT_TITLE, EventStatus.ENDED);

        given(userClientService.fetchUser()).willReturn(userInfo);
        given(eventRepository.findById(TEST_EVENT_ID)).willReturn(Optional.of(mockEvent));

        // when & then
        assertThatThrownBy(() -> ticketingService.processParticipationSuccess(TEST_EVENT_ID, CORRECT_PASSWORD, signatureImage))
                .isInstanceOf(TicketingException.class)
                .hasFieldOrPropertyWithValue("errorCode", TicketingErrorCode.EVENT_NOT_ACTIVE);

        verify(userClientService).fetchUser();
        verify(eventRepository).findById(TEST_EVENT_ID);
        verify(imageService, never()).handleImageUpload(any(MultipartFile.class));
    }

    @Test
    @DisplayName("티켓팅 유저 수령 처리 - 잘못된 비밀번호")
    void processParticipationSuccess_잘못된비밀번호() {
        // given
        UserInfoResponse userInfo = createUserInfo(TEST_USER_ID, TEST_USER_NAME);
        Event mockEvent = createMockEvent(TEST_EVENT_ID, TEST_EVENT_TITLE, EventStatus.ACTIVE);
        mockEvent = spy(mockEvent);
        given(mockEvent.getEventPassword()).willReturn(CORRECT_PASSWORD);

        given(userClientService.fetchUser()).willReturn(userInfo);
        given(eventRepository.findById(TEST_EVENT_ID)).willReturn(Optional.of(mockEvent));

        // when & then
        assertThatThrownBy(() -> ticketingService.processParticipationSuccess(TEST_EVENT_ID, WRONG_PASSWORD, signatureImage))
                .isInstanceOf(TicketingException.class)
                .hasFieldOrPropertyWithValue("errorCode", TicketingErrorCode.PASSWORD_INVALID);

        verify(userClientService).fetchUser();
        verify(eventRepository).findById(TEST_EVENT_ID);
        verify(imageService, never()).handleImageUpload(any(MultipartFile.class));
    }

    @Test
    @DisplayName("티켓팅 유저 수령 처리 - 참여 정보 없음")
    void processParticipationSuccess_참여정보없음() {
        // given
        UserInfoResponse userInfo = createUserInfo(TEST_USER_ID, TEST_USER_NAME);
        Event mockEvent = createMockEvent(TEST_EVENT_ID, TEST_EVENT_TITLE, EventStatus.ACTIVE);
        mockEvent = spy(mockEvent);
        given(mockEvent.getEventPassword()).willReturn(CORRECT_PASSWORD);

        given(userClientService.fetchUser()).willReturn(userInfo);
        given(eventRepository.findById(TEST_EVENT_ID)).willReturn(Optional.of(mockEvent));
        given(imageService.handleImageUpload(signatureImage)).willReturn(TEST_SIGNATURE_URL);
        given(participationRepository.findByEventAndUserId(mockEvent, TEST_USER_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> ticketingService.processParticipationSuccess(TEST_EVENT_ID, CORRECT_PASSWORD, signatureImage))
                .isInstanceOf(TicketingException.class)
                .hasFieldOrPropertyWithValue("errorCode", TicketingErrorCode.PARTICIPATION_NOT_FOUND);

        verify(imageService).handleImageUpload(signatureImage);
        verify(participationRepository).findByEventAndUserId(mockEvent, TEST_USER_ID);
    }

    @Test
    @DisplayName("티켓팅 유저 수령 처리 - 상태 변경 불가")
    void processParticipationSuccess_상태변경불가() {
        // given
        UserInfoResponse userInfo = createUserInfo(TEST_USER_ID, TEST_USER_NAME);
        Event mockEvent = createMockEvent(TEST_EVENT_ID, TEST_EVENT_TITLE, EventStatus.ACTIVE);
        mockEvent = spy(mockEvent);
        given(mockEvent.getEventPassword()).willReturn(CORRECT_PASSWORD);

        Participation mockParticipation = createMockParticipation(mockEvent, userInfo, ParticipationStatus.COMPLETED);

        given(userClientService.fetchUser()).willReturn(userInfo);
        given(eventRepository.findById(TEST_EVENT_ID)).willReturn(Optional.of(mockEvent));
        given(imageService.handleImageUpload(signatureImage)).willReturn(TEST_SIGNATURE_URL);
        given(participationRepository.findByEventAndUserId(mockEvent, TEST_USER_ID)).willReturn(Optional.of(mockParticipation));

        // when & then
        assertThatThrownBy(() -> ticketingService.processParticipationSuccess(TEST_EVENT_ID, CORRECT_PASSWORD, signatureImage))
                .isInstanceOf(TicketingException.class)
                .hasFieldOrPropertyWithValue("errorCode", TicketingErrorCode.CANNOT_CHANGE_STATUS);

        verify(participationRepository).findByEventAndUserId(mockEvent, TEST_USER_ID);
        verify(mockParticipation, never()).changeStatusCompleted(TEST_SIGNATURE_URL);
    }

    @Test
    @DisplayName("티켓팅 참여 취소 - 정상")
    void changeParticipationStatusCanceled_성공() {
        // given
        UserInfoResponse userInfo = createUserInfo(TEST_USER_ID, TEST_USER_NAME);
        Event mockEvent = createMockEvent(TEST_EVENT_ID, TEST_EVENT_TITLE, EventStatus.ACTIVE);

        Participation mockParticipation = createMockParticipation(mockEvent, userInfo, ParticipationStatus.WAITING);
        Stock mockStock = createMockStock(mockEvent, 10);

        given(userClientService.fetchUser()).willReturn(userInfo);
        given(eventRepository.findById(TEST_EVENT_ID)).willReturn(Optional.of(mockEvent));
        given(participationRepository.findByEventAndUserId(mockEvent, TEST_USER_ID)).willReturn(Optional.of(mockParticipation));
        given(stockRepository.findByEvent(mockEvent)).willReturn(Optional.of(mockStock));

        // when
        ticketingService.changeParticipationStatusCanceled(TEST_EVENT_ID);

        // then
        verify(userClientService).fetchUser();
        verify(eventRepository).findById(TEST_EVENT_ID);
        verify(participationRepository).findByEventAndUserId(mockEvent, TEST_USER_ID);
        verify(stockRepository).findByEvent(mockEvent);
        verify(mockParticipation).changeStatusCanceled();
        verify(mockStock).increase();

        verify(eventPublisher).publishEvent(any(ParticipationStatusChangedEvent.class));
    }

    @Test
    @DisplayName("티켓팅 참여 취소 - 이벤트 없음")
    void changeParticipationStatusCanceled_이벤트없음() {
        // given
        UserInfoResponse userInfo = createUserInfo(TEST_USER_ID, TEST_USER_NAME);
        given(userClientService.fetchUser()).willReturn(userInfo);
        given(eventRepository.findById(NON_EXISTENT_EVENT_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> ticketingService.changeParticipationStatusCanceled(NON_EXISTENT_EVENT_ID))
                .isInstanceOf(TicketingException.class)
                .hasFieldOrPropertyWithValue("errorCode", TicketingErrorCode.EVENT_NOT_FOUND);

        verify(userClientService).fetchUser();
        verify(eventRepository).findById(NON_EXISTENT_EVENT_ID);
        verify(participationRepository, never()).findByEventAndUserId(any(), any());
    }

    @Test
    @DisplayName("티켓팅 참여 취소 - 이벤트 시작 전")
    void changeParticipationStatusCanceled_이벤트비활성화() {
        // given
        UserInfoResponse userInfo = createUserInfo(TEST_USER_ID, TEST_USER_NAME);
        Event mockEvent = createMockEvent(TEST_EVENT_ID, TEST_EVENT_TITLE, EventStatus.UPCOMING);

        given(userClientService.fetchUser()).willReturn(userInfo);
        given(eventRepository.findById(TEST_EVENT_ID)).willReturn(Optional.of(mockEvent));

        // when & then
        assertThatThrownBy(() -> ticketingService.changeParticipationStatusCanceled(TEST_EVENT_ID))
                .isInstanceOf(TicketingException.class)
                .hasFieldOrPropertyWithValue("errorCode", TicketingErrorCode.EVENT_NOT_ACTIVE);

        verify(userClientService).fetchUser();
        verify(eventRepository).findById(TEST_EVENT_ID);
        verify(participationRepository, never()).findByEventAndUserId(any(), any());
    }

    @Test
    @DisplayName("티켓팅 참여 취소 - 유저 참여자 정보 없음")
    void changeParticipationStatusCanceled_참여정보없음() {
        // given
        UserInfoResponse userInfo = createUserInfo(TEST_USER_ID, TEST_USER_NAME);
        Event mockEvent = createMockEvent(TEST_EVENT_ID, TEST_EVENT_TITLE, EventStatus.ACTIVE);

        given(userClientService.fetchUser()).willReturn(userInfo);
        given(eventRepository.findById(TEST_EVENT_ID)).willReturn(Optional.of(mockEvent));
        given(participationRepository.findByEventAndUserId(mockEvent, TEST_USER_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> ticketingService.changeParticipationStatusCanceled(TEST_EVENT_ID))
                .isInstanceOf(TicketingException.class)
                .hasFieldOrPropertyWithValue("errorCode", TicketingErrorCode.PARTICIPATION_NOT_FOUND);

        verify(participationRepository).findByEventAndUserId(mockEvent, TEST_USER_ID);
        verify(stockRepository, never()).findByEvent(any());
    }

    @Test
    @DisplayName("티켓팅 참여 취소 - 상태 변경 불가")
    void changeParticipationStatusCanceled_상태변경불가() {
        // given
        UserInfoResponse userInfo = createUserInfo(TEST_USER_ID, TEST_USER_NAME);
        Event mockEvent = createMockEvent(TEST_EVENT_ID, TEST_EVENT_TITLE, EventStatus.ACTIVE);
        Participation mockParticipation = createMockParticipation(mockEvent, userInfo, ParticipationStatus.COMPLETED);

        given(userClientService.fetchUser()).willReturn(userInfo);
        given(eventRepository.findById(TEST_EVENT_ID)).willReturn(Optional.of(mockEvent));
        given(participationRepository.findByEventAndUserId(mockEvent, TEST_USER_ID)).willReturn(Optional.of(mockParticipation));

        // when & then
        assertThatThrownBy(() -> ticketingService.changeParticipationStatusCanceled(TEST_EVENT_ID))
                .isInstanceOf(TicketingException.class)
                .hasFieldOrPropertyWithValue("errorCode", TicketingErrorCode.CANNOT_CHANGE_STATUS);

        verify(participationRepository).findByEventAndUserId(mockEvent, TEST_USER_ID);
        verify(stockRepository, never()).findByEvent(any());
        verify(mockParticipation, never()).changeStatusCanceled();
    }

    @Test
    @DisplayName("티켓팅 참여 취소 - 재고 정보 없음")
    void changeParticipationStatusCanceled_재고정보없음() {
        // given
        UserInfoResponse userInfo = createUserInfo(TEST_USER_ID, TEST_USER_NAME);
        Event mockEvent = createMockEvent(TEST_EVENT_ID, TEST_EVENT_TITLE, EventStatus.ACTIVE);
        Participation mockParticipation = createMockParticipation(mockEvent, userInfo, ParticipationStatus.WAITING);

        given(userClientService.fetchUser()).willReturn(userInfo);
        given(eventRepository.findById(TEST_EVENT_ID)).willReturn(Optional.of(mockEvent));
        given(participationRepository.findByEventAndUserId(mockEvent, TEST_USER_ID)).willReturn(Optional.of(mockParticipation));
        given(stockRepository.findByEvent(mockEvent)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> ticketingService.changeParticipationStatusCanceled(TEST_EVENT_ID))
                .isInstanceOf(TicketingException.class)
                .hasFieldOrPropertyWithValue("errorCode", TicketingErrorCode.STOCK_NOT_FOUND);

        verify(stockRepository).findByEvent(mockEvent);
        verify(mockParticipation, never()).changeStatusCanceled();
    }

    private UserInfoResponse createUserInfo(String userId, String name) {
        return UserInfoResponse.builder()
                .userId(userId)
                .name(name)
                .studentId(TEST_STUDENT_ID)
                .build();
    }

    private Event createMockEvent(Long eventId, String title, EventStatus status) {
        Event event = Event.builder()
                .id(eventId)
                .title(title)
                .userId(ADMIN_USER_ID)
                .campus(Campus.SONGDO_CAMPUS)
                .eventTime(LocalDateTime.now().plusDays(1))
                .eventEndTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .description(TEST_EVENT_DESCRIPTION)
                .locationInfo(TEST_LOCATION)
                .target(TEST_TARGET)
                .build();

        event.updateStatus(status);
        return event;
    }

    private Participation createMockParticipation(Event event, UserInfoResponse userInfo, ParticipationStatus status) {
        Participation participation = spy(Participation.builder()
                .event(event)
                .ticketNumber(1)
                .userInfoResponse(userInfo)
                .build());

        given(participation.getStatus()).willReturn(status);
        return participation;
    }

    private Stock createMockStock(Event event, int currentStock) {
        Stock stock = spy(Stock.builder()
                .event(event)
                .initialStock(INITIAL_STOCK)
                .build());

        stock.updateStock(currentStock);
        return stock;
    }
}