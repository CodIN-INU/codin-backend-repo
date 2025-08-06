package inu.codin.codinticketingapi.domain.ticketing.service;

import inu.codin.codinticketingapi.domain.admin.entity.Event;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.EventDetailResponse;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.EventPageResponse;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.EventParticipationHistoryDto;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.EventParticipationHistoryPageResponse;
import inu.codin.codinticketingapi.domain.ticketing.entity.Campus;
import inu.codin.codinticketingapi.domain.ticketing.entity.ParticipationStatus;
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
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @InjectMocks
    private EventService eventService;

    @Mock
    private EventRepository eventRepository;
    @Mock
    private ParticipationRepository participationRepository;
    @Mock
    private UserClientService userClientService;

    private static final Campus SONGDO = Campus.SONGDO_CAMPUS;
    private static final Campus MICHUHOL = Campus.MICHUHOL_CAMPUS;
    private static final int PAGE_SIZE = 10;
    private static final Sort DESC_SORT = Sort.by("createdAt").descending();
    private static final String TEST_EVENT_TITLE = "TEST_EVENT_TITLE";
    private static final String TEST_USER_ID_1 = "user123";
    private static final String USER_456 = "user456";
    private static final String TEST_IMAGE_URL = "http://test-image.com";
    private static final String TEST_LOCATION = "테스트 장소";
    private static final String TEST_DESCRIPTION = "테스트 이벤트 설명";
    private static final int INITIAL_STOCK = 100;
    private static final LocalDateTime START_TIME = LocalDateTime.now().plusDays(1);
    private static final LocalDateTime END_TIME = LocalDateTime.now().plusDays(1).plusHours(2);

    @Test
    @DisplayName("이벤트 리스트 반환 - 정상, 페이징 메타 정보 검증")
    void getEventList_성공() {
        // given
        int page = 0;
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, DESC_SORT);
        Event mockEvent = createMockEvent(1L, TEST_EVENT_TITLE, SONGDO);
        Page<Event> mockPage = new PageImpl<>(List.of(mockEvent), pageable, 1);

        given(eventRepository.findByCampus(SONGDO, pageable)).willReturn(mockPage);

        // when
        EventPageResponse result = eventService.getEventList(SONGDO, page);

        // then
        assertThat(result.getEventList()).hasSize(1);
        assertThat(result.getLastPage()).isEqualTo(0);
        assertThat(result.getNextPage()).isEqualTo(-1);
        verify(eventRepository).findByCampus(SONGDO, pageable);
    }

    @Test
    @DisplayName("이벤트 리스트 반환 - 빈 리스트")
    void getEventList_빈리스트() {
        // given
        int page = 0;
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, DESC_SORT);
        Page<Event> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        given(eventRepository.findByCampus(MICHUHOL, pageable)).willReturn(emptyPage);

        // when
        EventPageResponse result = eventService.getEventList(MICHUHOL, page);

        // then
        assertThat(result.getEventList()).isEmpty();
        assertThat(result.getLastPage()).isEqualTo(-1);
        assertThat(result.getNextPage()).isEqualTo(-1);
        verify(eventRepository).findByCampus(MICHUHOL, pageable);
    }

    @Test
    @DisplayName("이벤트 리스트 반환 - Campus NULL 일때")
    void getEventList_campusNull() {
        // when & then
        assertThatThrownBy(() -> eventService.getEventList(null, 0))
                .isInstanceOf(NullPointerException.class);
    }


    @Test
    @DisplayName("이벤트 리스트 반환 - 페이지 음수 예외")
    void getEventList_페이지음수예외처리() {
        // when & then
        assertThatThrownBy(() -> eventService.getEventList(SONGDO, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("이벤트 리스트 반환 – 기본 페이지 크기(10) 로 호출되는지 검증")
    void getEventList_기본페이지크기() {
        // given
        int page = 2;
        Pageable expected = PageRequest.of(page, 10, Sort.by("createdAt").descending());
        given(eventRepository.findByCampus(eq(SONGDO), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(), expected, 0));

        // when
        eventService.getEventList(SONGDO, page);

        // then
        verify(eventRepository, times(1))
                .findByCampus(eq(SONGDO), argThat(p -> p.getPageSize() == 10 && p.getPageNumber() == page));
    }


    @Test
    @DisplayName("이벤트 상세 조회 - 성공")
    void getEventDetail_성공() {
        // given
        Long eventId = 999L;
        Event mockEvent = createMockEvent(eventId, TEST_EVENT_TITLE, SONGDO);
        given(eventRepository.findById(eventId)).willReturn(Optional.of(mockEvent));

        // when
        EventDetailResponse result = eventService.getEventDetail(eventId);

        // then
        assertThat(result.getEventId()).isEqualTo(eventId);
        assertThat(result.getEventTitle()).isEqualTo(TEST_EVENT_TITLE);
        verify(eventRepository).findById(eventId);
    }

    @Test
    @DisplayName("이벤트 상세 조회 - 이벤트 없음 예외")
    void getEventDetail_이벤트존재X() {
        // given
        Long eventId = 999L;
        given(eventRepository.findById(eventId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> eventService.getEventDetail(eventId))
                .isInstanceOf(TicketingException.class)
                .hasFieldOrPropertyWithValue("errorCode", TicketingErrorCode.EVENT_NOT_FOUND);

        verify(eventRepository).findById(eventId);
    }

    @Test
    @DisplayName("사용자 이벤트 참여 내역 조회 - 정상, 페이징 메타 정보 검증")
    void getUserEventList_성공() {
        // given
        int pageNumber = 1;
        Pageable pageable = PageRequest.of(0, PAGE_SIZE, DESC_SORT);
        UserInfoResponse userInfo = UserInfoResponse.builder().userId(TEST_USER_ID_1).build();
        EventParticipationHistoryDto historyDto = createMockHistoryDto(1L, "TEST HISTORY TITLE", ParticipationStatus.WAITING);
        Page<EventParticipationHistoryDto> mockPage = new PageImpl<>(List.of(historyDto), pageable, 1);

        given(userClientService.fetchUser()).willReturn(userInfo);
        given(participationRepository.findHistoryByUserId(TEST_USER_ID_1, pageable)).willReturn(mockPage);

        EventParticipationHistoryPageResponse result = eventService.getUserEventList(pageNumber);

        assertThat(result.getEventList()).hasSize(1);
        assertThat(result.getLastPage()).isEqualTo(0);
        assertThat(result.getNextPage()).isEqualTo(-1);
        verify(userClientService).fetchUser();
        verify(participationRepository).findHistoryByUserId(TEST_USER_ID_1, pageable);
    }

    @Test
    @DisplayName("사용자 이벤트 참여 내역 조회 - 페이지 음수 예외")
    void getUserEventList_페이지음수() {
        UserInfoResponse mockUser = UserInfoResponse.builder().userId("testUser").build();
        given(userClientService.fetchUser()).willReturn(mockUser);

        assertThatThrownBy(() -> eventService.getUserEventList(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("사용자 이벤트 참여 내역 조회 – 기본 페이지 크기(10)로 호출되는지 검증")
    void getUserEventList_기본페이지크기() {
        // given
        given(userClientService.fetchUser()).willReturn(UserInfoResponse.builder().userId(TEST_USER_ID_1).build());
        Pageable expected = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        given(participationRepository.findHistoryByUserId(eq(TEST_USER_ID_1), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(), expected, 0));

        // when
        eventService.getUserEventList(1);

        // then
        verify(participationRepository)
                .findHistoryByUserId(eq(TEST_USER_ID_1), argThat(p -> p.getPageSize() == 10 && p.getPageNumber() == 0));
    }

    @Test
    @DisplayName("상태별 사용자 이벤트 참여 내역 조회 - 완료, 취소, 대기")
    void getUserEventListByStatus_상태별() {
        int pageNumber = 1;
        Pageable pageable = PageRequest.of(0, PAGE_SIZE, DESC_SORT);
        UserInfoResponse userInfo = UserInfoResponse.builder().userId(USER_456).build();

        for (ParticipationStatus status : ParticipationStatus.values()) {
            // given
            Page<EventParticipationHistoryDto> mockPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            given(userClientService.fetchUser()).willReturn(userInfo);
            given(participationRepository.findHistoryByUserIdAndCanceled(USER_456, status, pageable)).willReturn(mockPage);

            // when
            EventParticipationHistoryPageResponse result = eventService.getUserEventListByStatus(pageNumber, status);

            // then
            assertThat(result.getEventList()).isEmpty();
            assertThat(result.getLastPage()).isEqualTo(0);
            assertThat(result.getNextPage()).isEqualTo(-1);
            verify(userClientService).fetchUser();
            verify(participationRepository).findHistoryByUserIdAndCanceled(USER_456, status, pageable);

            reset(userClientService, participationRepository);
        }
    }

    @Test
    @DisplayName("상태별 사용자 이벤트 참여 내역 조회 – Status Null")
    void getUserEventListByStatus_statusNull() {
        UserInfoResponse mockUser = UserInfoResponse.builder().userId(TEST_USER_ID_1).build();
        given(userClientService.fetchUser()).willReturn(mockUser);

        assertThatThrownBy(() -> eventService.getUserEventListByStatus(1, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("상태별 사용자 이벤트 참여 내역 조회 – 기본 페이지 크기(10)로 호출되는지 검증")
    void getUserEventListByStatus_기본페이지크기() {
        // given
        ParticipationStatus status = ParticipationStatus.COMPLETED;
        given(userClientService.fetchUser()).willReturn(UserInfoResponse.builder().userId(TEST_USER_ID_1).build());

        Pageable expected = PageRequest.of(1, 10, Sort.by("createdAt").descending());
        given(participationRepository.findHistoryByUserIdAndCanceled(eq(TEST_USER_ID_1), eq(status), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(), expected, 0));

        // when
        eventService.getUserEventListByStatus(2, status);

        // then
        verify(participationRepository).findHistoryByUserIdAndCanceled(
                eq(TEST_USER_ID_1),
                eq(status),
                argThat(p -> p.getPageSize() == 10 && p.getPageNumber() == 1)
        );
    }

    private Event createMockEvent(Long id, String title, Campus campus) {
        Stock stock = Stock.builder().initialStock(INITIAL_STOCK).build();
        return Event.builder()
                .id(id)
                .title(title)
                .campus(campus)
                .eventTime(START_TIME)
                .eventEndTime(END_TIME)
                .description(TEST_DESCRIPTION)
                .locationInfo(TEST_LOCATION)
                .target("테스트 대상")
                .eventImageUrl(TEST_IMAGE_URL)
                .stock(stock)
                .build();
    }

    private EventParticipationHistoryDto createMockHistoryDto(Long eventId, String title, ParticipationStatus status) {
        return new EventParticipationHistoryDto(
                eventId, title, TEST_IMAGE_URL, TEST_LOCATION, START_TIME, END_TIME, status
        );
    }
}