package inu.codin.codinticketingapi.domain.ticketing.service;

import inu.codin.codinticketingapi.domain.admin.entity.Event;
import inu.codin.codinticketingapi.domain.admin.entity.EventStatus;
import inu.codin.codinticketingapi.domain.ticketing.dto.event.ParticipationCreatedEvent;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.ParticipationResponse;
import inu.codin.codinticketingapi.domain.ticketing.entity.*;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingErrorCode;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingException;
import inu.codin.codinticketingapi.domain.ticketing.redis.RedisParticipationService;
import inu.codin.codinticketingapi.domain.ticketing.repository.EventRepository;
import inu.codin.codinticketingapi.domain.ticketing.repository.ParticipationRepository;
import inu.codin.codinticketingapi.domain.user.dto.UserInfoResponse;
import inu.codin.codinticketingapi.domain.user.exception.UserErrorCode;
import inu.codin.codinticketingapi.domain.user.exception.UserException;
import inu.codin.codinticketingapi.domain.user.service.UserClientService;
import inu.codin.codinticketingapi.security.util.SecurityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
    private EventRepository eventRepository;
    @Mock
    private ParticipationRepository participationRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private RedisParticipationService redisParticipationService;

    private static final Long EVENT_ID = 1L;
    private static final String USER_ID = "testUser";
    private static final String USER_NAME = "테스트사용자";
    private static final String STUDENT_ID = "202012345";
    private static final int TICKET_NUMBER = 1;

    @Test
    @DisplayName("참여 저장 - 정상")
    void saveParticipation_성공() {
        // given
        UserInfoResponse userInfo = UserInfoResponse.builder()
                .userId(USER_ID)
                .name(USER_NAME)
                .studentId(STUDENT_ID)
                .department(Department.COMPUTER_SCI)
                .build();

        Event event = Event.builder()
                .id(EVENT_ID)
                .title("테스트 이벤트")
                .campus(Campus.SONGDO_CAMPUS)
                .eventTime(LocalDateTime.now().minusDays(1))
                .eventEndTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .build();

        event.updateStatus(EventStatus.ACTIVE);

        Stock stock = Stock.builder()
                .event(event)
                .initialStock(100)
                .build();
        stock.updateStock(99);

        Participation participation = Participation.builder()
                .event(event)
                .ticketNumber(TICKET_NUMBER)
                .userInfoResponse(userInfo)
                .build();

        given(userClientService.fetchUser()).willReturn(userInfo);
        given(eventRepository.findById(EVENT_ID)).willReturn(Optional.of(event));
        given(redisParticipationService.getCachedParticipation(USER_ID, EVENT_ID)).willReturn(Optional.empty());
        given(participationRepository.findByUserIdAndEvent(USER_ID, event)).willReturn(Optional.empty());
        given(participationRepository.save(any(Participation.class))).willReturn(participation);

        // when
        ParticipationResponse result = participationService.saveParticipation(EVENT_ID);

        // then
        assertThat(result.getTicketNumber()).isEqualTo(TICKET_NUMBER);
        verify(eventPublisher).publishEvent(any(ParticipationCreatedEvent.class));
    }

    @Test
    @DisplayName("참여 저장 - 유저 정보 부족")
    void saveParticipation_유저정보부족() {
        // given
        UserInfoResponse userInfo = UserInfoResponse.builder()
                .userId(USER_ID)
                .name(USER_NAME)
                .build(); // department, studentId 없음

        given(userClientService.fetchUser()).willReturn(userInfo);

        // when & then
        assertThatThrownBy(() -> participationService.saveParticipation(EVENT_ID))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.NOT_EXIST_PARTICIPATION_DATA);
    }

    @Test
    @DisplayName("참여 저장 - 이벤트 없음")
    void saveParticipation_이벤트없음() {
        // given
        UserInfoResponse userInfo = UserInfoResponse.builder()
                .userId(USER_ID)
                .department(Department.COMPUTER_SCI)
                .studentId(STUDENT_ID)
                .build();

        given(userClientService.fetchUser()).willReturn(userInfo);
        given(eventRepository.findById(EVENT_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> participationService.saveParticipation(EVENT_ID))
                .isInstanceOf(TicketingException.class)
                .hasFieldOrPropertyWithValue("errorCode", TicketingErrorCode.EVENT_NOT_FOUND);
    }

    @Test
    @DisplayName("참여 저장 - 캐시에서 조회")
    void saveParticipation_캐시조회() {
        // given
        UserInfoResponse userInfo = UserInfoResponse.builder()
                .userId(USER_ID)
                .department(Department.COMPUTER_SCI)
                .studentId(STUDENT_ID)
                .build();

        ParticipationResponse cachedResponse = ParticipationResponse.builder()
                .ticketNumber(TICKET_NUMBER)
                .status(ParticipationStatus.WAITING)
                .build();

        Event event = Event.builder()
                .id(EVENT_ID)
                .title("테스트 이벤트")
                .campus(Campus.SONGDO_CAMPUS)
                .eventTime(LocalDateTime.now().plusDays(1))
                .eventEndTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .build();

        given(userClientService.fetchUser()).willReturn(userInfo);
        given(eventRepository.findById(EVENT_ID)).willReturn(Optional.of(event));
        given(redisParticipationService.getCachedParticipation(USER_ID, EVENT_ID)).willReturn(Optional.of(cachedResponse));

        // when
        ParticipationResponse result = participationService.saveParticipation(EVENT_ID);

        // then
        assertThat(result.getTicketNumber()).isEqualTo(TICKET_NUMBER);
        verify(userClientService).fetchUser();
        verify(eventRepository).findById(EVENT_ID);
        verify(redisParticipationService).getCachedParticipation(USER_ID, EVENT_ID);
        verify(participationRepository, never()).findByUserIdAndEvent(any(), any());
        verify(ticketingService, never()).decrement(any());
    }

    @Test
    @DisplayName("참여 저장 - 이미 참여한 경우")
    void saveParticipation_이미참여() {
        // given
        UserInfoResponse userInfo = UserInfoResponse.builder()
                .userId(USER_ID)
                .department(Department.COMPUTER_SCI)
                .studentId(STUDENT_ID)
                .build();

        Event event = Event.builder().id(EVENT_ID).build();

        Participation existingParticipation = Participation.builder()
                .event(event)
                .ticketNumber(TICKET_NUMBER)
                .userInfoResponse(userInfo)
                .build();

        given(userClientService.fetchUser()).willReturn(userInfo);
        given(eventRepository.findById(EVENT_ID)).willReturn(Optional.of(event));
        given(redisParticipationService.getCachedParticipation(USER_ID, EVENT_ID)).willReturn(Optional.empty());
        given(participationRepository.findByUserIdAndEvent(USER_ID, event)).willReturn(Optional.of(existingParticipation));

        // when
        ParticipationResponse result = participationService.saveParticipation(EVENT_ID);

        // then
        assertThat(result.getTicketNumber()).isEqualTo(TICKET_NUMBER);
        verify(ticketingService, never()).decrement(any());
        verify(participationRepository, never()).save(any());
    }

    @Test
    @DisplayName("이벤트별 참여 조회 - 정상")
    void findParticipationByEvent_성공() {
        // given
        UserInfoResponse userInfo = UserInfoResponse.builder()
                .userId(USER_ID)
                .department(Department.COMPUTER_SCI)
                .studentId(STUDENT_ID)
                .build();

        Event event = Event.builder()
                .id(EVENT_ID)
                .build();

        Participation participation = Participation.builder()
                .event(event)
                .ticketNumber(TICKET_NUMBER)
                .userInfoResponse(userInfo)
                .build();

        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getUserId).thenReturn(USER_ID);
            given(redisParticipationService.getCachedParticipation(USER_ID, EVENT_ID)).willReturn(Optional.empty());
            given(eventRepository.findById(EVENT_ID)).willReturn(Optional.of(event));
            given(participationRepository.findByUserIdAndEvent(USER_ID, event)).willReturn(Optional.of(participation));

            // when
            ParticipationResponse result = participationService.findParticipationByEvent(EVENT_ID);

            // then
            assertThat(result.getTicketNumber()).isEqualTo(TICKET_NUMBER);
            verify(redisParticipationService).cacheParticipation(USER_ID, EVENT_ID, participation);
        }
    }

    @Test
    @DisplayName("이벤트별 참여 조회 - 캐시에서 조회")
    void findParticipationByEvent_캐시조회() {
        // given
        ParticipationResponse cachedResponse = ParticipationResponse.builder()
                .ticketNumber(TICKET_NUMBER)
                .status(ParticipationStatus.WAITING)
                .build();

        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getUserId).thenReturn(USER_ID);
            given(redisParticipationService.getCachedParticipation(USER_ID, EVENT_ID)).willReturn(Optional.of(cachedResponse));

            // when
            ParticipationResponse result = participationService.findParticipationByEvent(EVENT_ID);

            // then
            assertThat(result.getTicketNumber()).isEqualTo(TICKET_NUMBER);
            verify(eventRepository, never()).findById(any());
            verify(participationRepository, never()).findByUserIdAndEvent(any(), any());
        }
    }

    @Test
    @DisplayName("이벤트별 참여 조회 - 이벤트 없음")
    void findParticipationByEvent_이벤트없음() {
        // given
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getUserId).thenReturn(USER_ID);
            given(redisParticipationService.getCachedParticipation(USER_ID, EVENT_ID)).willReturn(Optional.empty());
            given(eventRepository.findById(EVENT_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> participationService.findParticipationByEvent(EVENT_ID))
                    .isInstanceOf(TicketingException.class)
                    .hasFieldOrPropertyWithValue("errorCode", TicketingErrorCode.EVENT_NOT_FOUND);
        }
    }

    @Test
    @DisplayName("이벤트별 참여 조회 - 참여 정보 없음")
    void findParticipationByEvent_참여정보없음() {
        // given
        Event event = Event.builder().id(EVENT_ID).build();

        // SecurityUtil 이라는 SecurityContext에서 데이터를 가져오는 static 클래스이기에 MockedStatic 사용
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getUserId).thenReturn(USER_ID);
            given(redisParticipationService.getCachedParticipation(USER_ID, EVENT_ID)).willReturn(Optional.empty());
            given(eventRepository.findById(EVENT_ID)).willReturn(Optional.of(event));
            given(participationRepository.findByUserIdAndEvent(USER_ID, event)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> participationService.findParticipationByEvent(EVENT_ID))
                    .isInstanceOf(TicketingException.class)
                    .hasFieldOrPropertyWithValue("errorCode", TicketingErrorCode.PARTICIPATION_NOT_FOUND);
        }
    }
}