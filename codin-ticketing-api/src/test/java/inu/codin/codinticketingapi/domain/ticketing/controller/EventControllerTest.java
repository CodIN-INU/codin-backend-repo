package inu.codin.codinticketingapi.domain.ticketing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private EventStockProducerService eventStockProducerService;

    @BeforeEach
    void setUp() {
        TokenUserDetails userDetails = TokenUserDetails.builder()
                .userId("TEST_USER_ID")
                .email("testuser@inu.ac.kr")
                .token("TEST_TOKEN")
                .role("USER")
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("이벤트 목록 조회 - 성공")
    void getEventList_Success() throws Exception {
        // given
        EventPageDetailResponse eventDetail = EventPageDetailResponse.builder()
                .eventId(1L)
                .eventTitle("테스트 이벤트")
                .eventImageUrl("test-image-url")
                .eventTime(LocalDateTime.now().plusHours(1))
                .eventEndTime(LocalDateTime.now().plusHours(2))
                .locationInfo("테스트 위치")
                .quantity(100)
                .currentQuantity(80)
                .build();

        EventPageResponse eventPageResponse = EventPageResponse.of(
                List.of(eventDetail), 0, -1
        );

        when(eventService.getEventList(any(Campus.class), anyInt()))
                .thenReturn(eventPageResponse);

        // when & then
        mockMvc.perform(get("/event")
                        .param("campus", "SONGDO_CAMPUS")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eventList").isArray())
                .andExpect(jsonPath("$.data.eventList[0].eventId").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("이벤트 상세 조회 - 성공")
    void getEventDetail_Success() throws Exception {
        // given
        EventDetailResponse eventDetailResponse = EventDetailResponse.builder()
                .eventId(1L)
                .eventTime(LocalDateTime.now())
                .eventEndTime(LocalDateTime.now().plusHours(2))
                .eventImageUrls("test-image-url")
                .eventTitle("테스트 이벤트")
                .locationInfo("테스트 위치")
                .quantity(100)
                .currentQuantity(80)
                .target("테스트 대상")
                .description("테스트 설명")
                .build();

        when(eventService.getEventDetail(1L))
                .thenReturn(eventDetailResponse);

        // when & then
        mockMvc.perform(get("/event/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eventId").value(1))
                .andExpect(jsonPath("$.data.eventTitle").value("테스트 이벤트"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("유저 이벤트 참여 전체 이력 조회 - 성공")
    void getUserEventList_Success() throws Exception {
        // given
        EventParticipationHistoryDto historyDto = new EventParticipationHistoryDto(
                1L, "테스트 이벤트", "test-image-url", "테스트 위치",
                LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                ParticipationStatus.COMPLETED
        );

        EventParticipationHistoryPageResponse response = EventParticipationHistoryPageResponse.of(
                List.of(historyDto), 0, -1
        );

        when(eventService.getUserEventList(0))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/event/user")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eventList").isArray())
                .andExpect(jsonPath("$.data.eventList[0].eventId").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("유저 이벤트 참여 상태별 이력 조회 - 성공")
    void getUserEventListByStatus_Success() throws Exception {
        // given
        EventParticipationHistoryDto historyDto = new EventParticipationHistoryDto(
                1L, "테스트 이벤트", "test-image-url", "테스트 위치",
                LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                ParticipationStatus.COMPLETED
        );

        EventParticipationHistoryPageResponse response = EventParticipationHistoryPageResponse.of(
                List.of(historyDto), 0, -1
        );

        when(eventService.getUserEventListByStatus(anyInt(), any(ParticipationStatus.class)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/event/user/status")
                        .param("page", "0")
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eventList").isArray())
                .andExpect(jsonPath("$.data.eventList[0].status").value("COMPLETED"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "MANAGER"})
    @DisplayName("SSE 전송 - 권한별 성공 테스트")
    void sendSse_AsManager(String role) throws Exception {
        TokenUserDetails userDetails = TokenUserDetails.builder()
                .userId("TEST_USER_ID")
                .email("testuser@inu.ac.kr")
                .token("TEST_TOKEN")
                .role(role)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        // given
        doNothing().when(eventStockProducerService).publishEventStock(any());

        // when & then
        mockMvc.perform(post("/event/sse/{id}", 1L)
                        .param("quantity", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("SSE 전송 성공"));
        verify(eventStockProducerService).publishEventStock(any());
    }
}
