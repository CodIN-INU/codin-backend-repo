package inu.codin.codinticketingapi.domain.ticketing.controller;

import inu.codin.codinticketingapi.domain.ticketing.dto.response.*;
import inu.codin.codinticketingapi.domain.ticketing.entity.Campus;
import inu.codin.codinticketingapi.domain.ticketing.entity.ParticipationStatus;
import inu.codin.codinticketingapi.domain.ticketing.service.EventService;
import inu.codin.codinticketingapi.domain.ticketing.service.EventStockProducerService;
import inu.codin.codinticketingapi.security.jwt.TokenUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EventController.class)
@AutoConfigureMockMvc(addFilters = false)
class EventControllerTest {

    private static final Long EVENT_ID = 1000L;
    private static final String TEST_TITLE = "test-title";
    private static final String TEST_LOCATION = "test-location";
    private static final String TEST_IMAGE_URL = "test-image-url";
    private static final String TEST_TARGET = "test-target";
    private static final String TEST_DESCRIPTION = "test-description";
    private static final String TEST_USER_ID = "test-user-id";
    private static final String TEST_EMAIL = "testuser@inu.ac.kr";
    private static final String TEST_TOKEN = "test-token";
    private static final int QUANTITY = 100;
    private static final int CURRENT_QUANTITY = 80;
    private static final int PAGE = 0;
    private static final int LAST_PAGE = -1;
    private static final String CAMPUS_PARAM = "SONGDO_CAMPUS";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private EventStockProducerService eventStockProducerService;

    @BeforeEach
    void setUp() {
        TokenUserDetails userDetails = getTokenUserDetails("USER");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("이벤트 목록 조회 - 성공")
    void getEventList_Success() throws Exception {
        // given
        EventPageDetailResponse eventDetail = getEventPageDetailResponse();
        EventPageResponse eventPageResponse = EventPageResponse.of(List.of(eventDetail), PAGE, LAST_PAGE);
        // when
        when(eventService.getEventList(any(Campus.class), anyInt()))
                .thenReturn(eventPageResponse);
        // then
        mockMvc.perform(get("/event")
                        .param("campus", CAMPUS_PARAM)
                        .param("page", String.valueOf(PAGE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eventList").isArray())
                .andExpect(jsonPath("$.data.eventList[0].eventId").value(EVENT_ID));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("이벤트 상세 조회 - 성공")
    void getEventDetail_Success() throws Exception {
        // given
        EventDetailResponse eventDetailResponse = getEventDetailResponse();
        // when
        when(eventService.getEventDetail(EVENT_ID)).thenReturn(eventDetailResponse);
        // then
        mockMvc.perform(get("/event/{id}", EVENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eventId").value(EVENT_ID))
                .andExpect(jsonPath("$.data.eventTitle").value(TEST_TITLE));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("유저 이벤트 참여 전체 이력 조회 - 성공")
    void getUserEventList_Success() throws Exception {
        for (ParticipationStatus status : ParticipationStatus.values()) {
            // given
            EventParticipationHistoryDto historyDto = getEventParticipationHistoryDto(status);
            EventParticipationHistoryPageResponse response = EventParticipationHistoryPageResponse.of(List.of(historyDto), PAGE, LAST_PAGE);
            // when
            when(eventService.getUserEventList(PAGE))
                    .thenReturn(response);
            // then
            mockMvc.perform(get("/event/user")
                            .param("page", String.valueOf(PAGE)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.eventList").isArray())
                    .andExpect(jsonPath("$.data.eventList[0].eventId").value(EVENT_ID))
                    .andExpect(jsonPath("$.data.eventList[0].status").value(status.name()));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "MANAGER"})
    @DisplayName("SSE 전송 - 권한별 성공 테스트")
    void sendSse_AsManager(String role) throws Exception {
        TokenUserDetails userDetails = getTokenUserDetails(role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        // given
        doNothing().when(eventStockProducerService).publishEventStock(any());

        // when & then
        mockMvc.perform(post("/event/sse/{id}", EVENT_ID)
                        .param("quantity", String.valueOf(QUANTITY)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("SSE 전송 성공"));
        verify(eventStockProducerService).publishEventStock(any());
    }

    private static EventPageDetailResponse getEventPageDetailResponse() {
        return EventPageDetailResponse.builder()
                .eventId(EVENT_ID)
                .eventTitle(TEST_TITLE)
                .eventImageUrl(TEST_IMAGE_URL)
                .eventTime(LocalDateTime.now().plusHours(1))
                .eventEndTime(LocalDateTime.now().plusHours(2))
                .locationInfo(TEST_LOCATION)
                .quantity(QUANTITY)
                .currentQuantity(CURRENT_QUANTITY)
                .build();
    }

    private static TokenUserDetails getTokenUserDetails(String role) {
        return TokenUserDetails.builder()
                .userId(TEST_USER_ID)
                .email(TEST_EMAIL)
                .token(TEST_TOKEN)
                .role(role)
                .build();
    }

    private static EventDetailResponse getEventDetailResponse() {
        return EventDetailResponse.builder()
                .eventId(EVENT_ID)
                .eventTime(LocalDateTime.now())
                .eventEndTime(LocalDateTime.now().plusHours(2))
                .eventImageUrls(TEST_IMAGE_URL)
                .eventTitle(TEST_TITLE)
                .locationInfo(TEST_LOCATION)
                .quantity(QUANTITY)
                .currentQuantity(CURRENT_QUANTITY)
                .target(TEST_TARGET)
                .description(TEST_DESCRIPTION)
                .build();
    }

    private static EventParticipationHistoryDto getEventParticipationHistoryDto(ParticipationStatus status) {
        return EventParticipationHistoryDto.builder()
                .eventId(EVENT_ID)
                .title(TEST_TITLE)
                .eventImageUrl(TEST_IMAGE_URL)
                .locationInfo(TEST_LOCATION)
                .eventTime(LocalDateTime.now())
                .eventEndTime(LocalDateTime.now().plusHours(2))
                .eventReceivedStartTime(LocalDateTime.now().plusHours(1))
                .eventReceivedEndTime(LocalDateTime.now().plusHours(2))
                .status(status)
                .build();
    }
}
