package inu.codin.codinticketingapi.domain.ticketing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.ParticipationResponse;
import inu.codin.codinticketingapi.domain.ticketing.entity.ParticipationStatus;
import inu.codin.codinticketingapi.domain.ticketing.service.ParticipationService;
import inu.codin.codinticketingapi.domain.ticketing.service.TicketingService;
import inu.codin.codinticketingapi.security.jwt.TokenUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketingController.class)
@AutoConfigureMockMvc(addFilters = false)
class TicketingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TicketingService ticketingService;

    @MockitoBean
    private ParticipationService participationService;

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
    @DisplayName("티켓팅 이벤트 참여 - 성공")
    void createUserParticipation_Success() throws Exception {
        // given
        Long eventId = 1L;
        ParticipationResponse response = ParticipationResponse.builder()
                .status(ParticipationStatus.WAITING)
                .ticketNumber(1)
                .signatureImgUrl(null)
                .build();

        when(participationService.saveParticipation(eventId)).thenReturn(response);

        // when & then
        mockMvc.perform(post("/event/join/{eventId}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("티켓팅 이벤트 참여 및 교환권 부여 성공"))
                .andExpect(jsonPath("$.data.status").value("WAITING"))
                .andExpect(jsonPath("$.data.ticketNumber").value(1));

        verify(participationService).saveParticipation(eventId);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("티켓팅 수령 확인 - 성공")
    void updateParticipationStatusByPassword_Success() throws Exception {
        // given
        Long eventId = 1L;
        String adminPassword = "1234";
        MockMultipartFile signatureImage = new MockMultipartFile(
                "signatureImage",
                "signature.png",
                "image/png",
                "signature image content".getBytes()
        );
        MockMultipartFile passwordPart = new MockMultipartFile(
                "password",
                "",
                "text/plain",
                adminPassword.getBytes()
        );

        doNothing().when(ticketingService).processParticipationSuccess(eq(eventId), eq(adminPassword), any());

        // when & then
        mockMvc.perform(multipart("/event/complete/{eventId}", eventId)
                        .file(signatureImage)
                        .file(passwordPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("관리자 비밀번호로 수령 확인 성공"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(ticketingService).processParticipationSuccess(eq(eventId), eq(adminPassword), any());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("티켓팅 취소 - 성공")
    void updateStatusCanceledParticipation_Success() throws Exception {
        // given
        Long eventId = 1L;
        doNothing().when(ticketingService).changeParticipationStatusCanceled(eventId);

        // when & then
        mockMvc.perform(delete("/event/cancel/{eventId}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("티켓팅 취소 완료"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(ticketingService).changeParticipationStatusCanceled(eventId);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("티켓팅 이벤트 참여 - 잘못된 eventId")
    void createUserParticipation_InvalidEventId() throws Exception {
        // when & then
        mockMvc.perform(post("/event/join/{eventId}", "invalid"))
                .andExpect(status().isBadRequest());

        verify(participationService, never()).saveParticipation(any());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("티켓팅 수령 확인 - 서명 이미지 없음")
    void updateParticipationStatusByPassword_NoSignatureImage() throws Exception {
        // given
        Long eventId = 1L;
        MockMultipartFile passwordPart = new MockMultipartFile(
                "password",
                "",
                "text/plain",
                "1234".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/event/complete/{eventId}", eventId)
                        .file(passwordPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        verify(ticketingService, never()).processParticipationSuccess(any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("티켓팅 수령 확인 - 비밀번호 없음")
    void updateParticipationStatusByPassword_NoPassword() throws Exception {
        // given
        Long eventId = 1L;
        MockMultipartFile signatureImage = new MockMultipartFile(
                "signatureImage",
                "signature.png",
                "image/png",
                "signature image content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/event/complete/{eventId}", eventId)
                        .file(signatureImage)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        verify(ticketingService, never()).processParticipationSuccess(any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("티켓팅 취소 - 잘못된 eventId")
    void updateStatusCanceledParticipation_InvalidEventId() throws Exception {
        // when & then
        mockMvc.perform(delete("/event/cancel/{eventId}", "invalid"))
                .andExpect(status().isBadRequest());

        verify(ticketingService, never()).changeParticipationStatusCanceled(any());
    }
}