package inu.codin.codinticketingapi.domain.ticketing.service;

import inu.codin.codinticketingapi.domain.admin.entity.Event;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.ParticipationCreateResponse;
import inu.codin.codinticketingapi.domain.ticketing.entity.Campus;
import inu.codin.codinticketingapi.domain.ticketing.entity.Participation;
import inu.codin.codinticketingapi.domain.ticketing.entity.Stock;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingErrorCode;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingException;
import inu.codin.codinticketingapi.domain.ticketing.repository.EventRepository;
import inu.codin.codinticketingapi.domain.ticketing.repository.ParticipationRepository;
import inu.codin.codinticketingapi.domain.user.dto.UserInfoResponse;
import inu.codin.codinticketingapi.domain.user.service.UserClientService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipationServiceTest {

    @InjectMocks
    private ParticipationService participationService;

    @Mock
    private TicketingService ticketingService;
    @Mock
    private UserClientService userClientService;

    @Mock
    private ParticipationRepository participationRepository;
    @Mock
    private EventRepository eventRepository;

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
    private static final int INITIAL_STOCK = 100;
    private static final int CURRENT_STOCK_90 = 90;
    private static final int CURRENT_STOCK_85 = 85;
    private static final int EXPECTED_TICKET_NUMBER_10 = 10;
    private static final int EXPECTED_TICKET_NUMBER_15 = 15;

    @Test
    @DisplayName("참여자 정보 저장 - 정상")
    void saveParticipation_성공() {
        // given
        UserInfoResponse userInfo = UserInfoResponse.builder()
                .userId(TEST_USER_ID)
                .name(TEST_USER_NAME)
                .studentId(TEST_STUDENT_ID)
                .build();

        Event mockEvent = createMockEvent(TEST_EVENT_ID, TEST_EVENT_TITLE);
        Stock mockStock = Stock.builder()
                .event(mockEvent)
                .initialStock(INITIAL_STOCK)
                .build();
        mockStock.updateStock(CURRENT_STOCK_90);

        Participation savedParticipation = Participation.builder()
                .event(mockEvent)
                .ticketNumber(EXPECTED_TICKET_NUMBER_10)
                .userInfoResponse(userInfo)
                .build();

        given(userClientService.fetchUser()).willReturn(userInfo);
        given(eventRepository.findById(TEST_EVENT_ID)).willReturn(Optional.of(mockEvent));
        given(ticketingService.decrement(TEST_EVENT_ID)).willReturn(mockStock);
        given(participationRepository.save(any(Participation.class))).willReturn(savedParticipation);

        // when
        ParticipationCreateResponse result = participationService.saveParticipation(TEST_EVENT_ID);

        // then
        assertThat(result).isNotNull();
        verify(userClientService).fetchUser();
        verify(eventRepository).findById(TEST_EVENT_ID);
        verify(ticketingService).decrement(TEST_EVENT_ID);
        verify(participationRepository).save(any(Participation.class));
    }

    @Test
    @DisplayName("참여 저장 - 이벤트 없음 예외")
    void saveParticipation_이벤트존재X() {
        // given
        UserInfoResponse userInfo = UserInfoResponse.builder()
                .userId(TEST_USER_ID)
                .build();

        given(userClientService.fetchUser()).willReturn(userInfo);
        given(eventRepository.findById(NON_EXISTENT_EVENT_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> participationService.saveParticipation(NON_EXISTENT_EVENT_ID))
                .isInstanceOf(TicketingException.class)
                .hasFieldOrPropertyWithValue("errorCode", TicketingErrorCode.EVENT_NOT_FOUND);

        verify(userClientService).fetchUser();
        verify(eventRepository).findById(NON_EXISTENT_EVENT_ID);
        verify(ticketingService, never()).decrement(any());
        verify(participationRepository, never()).save(any());
    }

    @Test
    @DisplayName("참여 저장 - 재고 부족 예외")
    void saveParticipation_재고부족() {
        // given
        UserInfoResponse userInfo = UserInfoResponse.builder()
                .userId(TEST_USER_ID)
                .build();
        Event mockEvent = createMockEvent(TEST_EVENT_ID, TEST_EVENT_TITLE);

        given(userClientService.fetchUser()).willReturn(userInfo);
        given(eventRepository.findById(TEST_EVENT_ID)).willReturn(Optional.of(mockEvent));
        given(ticketingService.decrement(TEST_EVENT_ID)).willThrow(new TicketingException(TicketingErrorCode.SOLD_OUT));

        // when & then
        assertThatThrownBy(() -> participationService.saveParticipation(TEST_EVENT_ID))
                .isInstanceOf(TicketingException.class)
                .hasFieldOrPropertyWithValue("errorCode", TicketingErrorCode.SOLD_OUT);

        verify(userClientService).fetchUser();
        verify(eventRepository).findById(TEST_EVENT_ID);
        verify(ticketingService).decrement(TEST_EVENT_ID);
        verify(participationRepository, never()).save(any());
    }

    @Test
    @DisplayName("참여 저장 - 티켓 번호 정확성 검증")
    void saveParticipation_티켓번호검증() {
        // given
        UserInfoResponse userInfo = UserInfoResponse.builder()
                .userId(TEST_USER_ID)
                .name(TEST_USER_NAME)
                .studentId(TEST_STUDENT_ID)
                .build();

        Event mockEvent = createMockEvent(TEST_EVENT_ID, TEST_EVENT_TITLE);
        Stock mockStock = Stock.builder()
                .event(mockEvent)
                .initialStock(INITIAL_STOCK)
                .build();
        mockStock.updateStock(CURRENT_STOCK_85);

        given(userClientService.fetchUser()).willReturn(userInfo);
        given(eventRepository.findById(TEST_EVENT_ID)).willReturn(Optional.of(mockEvent));
        given(ticketingService.decrement(TEST_EVENT_ID)).willReturn(mockStock);
        given(participationRepository.save(any(Participation.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        participationService.saveParticipation(TEST_EVENT_ID);

        // then
        verify(participationRepository).save(argThat(participation ->
                participation.getTicketNumber().equals(EXPECTED_TICKET_NUMBER_15)
        ));
    }

    private Event createMockEvent(Long eventId, String title) {
        return Event.builder()
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
    }
}