package inu.codin.codin.domain.email.service;

import inu.codin.codin.domain.email.dto.JoinEmailCheckRequestDto;
import inu.codin.codin.domain.email.dto.JoinEmailSendRequestDto;
import inu.codin.codin.domain.email.entity.EmailAuthEntity;
import inu.codin.codin.domain.email.exception.EmailAuthFailException;
import inu.codin.codin.domain.email.repository.EmailAuthRepository;
import inu.codin.codin.domain.email.util.AuthNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JoinEmailAuthServiceTest {

    @InjectMocks
    JoinEmailAuthService joinEmailAuthService;

    @Mock
    EmailAuthRepository emailAuthRepository;
    @Mock
    EmailTemplateService emailTemplateService;
    @Mock
    AuthNumberGenerator authNumberGenerator;

    private JoinEmailSendRequestDto sendRequestDto;
    private JoinEmailCheckRequestDto checkRequestDto;
    private EmailAuthEntity mockEmailAuth;

    private final String testMail = "test@inu.ac.kr";
    private final String testAuthNum = "AUTH123";

    @BeforeEach
    void setUp() {
        sendRequestDto = JoinEmailSendRequestDto.builder()
                .email(testMail)
                .build();
        checkRequestDto = JoinEmailCheckRequestDto.builder()
                .email(testMail)
                .authNum(testAuthNum)
                .build();
        mockEmailAuth = EmailAuthEntity.builder()
                .email(testMail)
                .authNum(testAuthNum)
                .build();

        // BaseTimeEntity의 기본 생성 객체가 없기 때문에 null pointer 에러 발생
        mockEmailAuth.setUpdatedAt();
    }

    @Test
    @DisplayName("회원가입 인증 이메일 전송 - 성공 (신규 사용자)")
    void sendJoinAuthEmail_성공_신규사용자() {
        // given
        when(emailAuthRepository.findByEmail(testMail)).thenReturn(Optional.empty());
        when(authNumberGenerator.generate()).thenReturn("NEWAUTH");

        // when
        joinEmailAuthService.sendJoinAuthEmail(sendRequestDto);

        // then
        ArgumentCaptor<EmailAuthEntity> emailAuthCaptor = ArgumentCaptor.forClass(EmailAuthEntity.class);
        verify(emailAuthRepository).save(emailAuthCaptor.capture());
        
        EmailAuthEntity savedAuth = emailAuthCaptor.getValue();
        assertEquals(testMail, savedAuth.getEmail());
        assertEquals("NEWAUTH", savedAuth.getAuthNum());

        verify(emailTemplateService).sendTemplateEmail(
                eq(testMail),
                eq("[CODIN] 회원가입 인증번호입니다."),
                eq("auth-email"),
                eq("NEWAUTH")
        );
    }

    @Test
    @DisplayName("회원가입 인증 이메일 전송 - 성공 (기존 인증 정보 갱신)")
    void sendJoinAuthEmail_성공_기존인증정보갱신() {
        // given
        EmailAuthEntity existingAuth = EmailAuthEntity.builder()
                .email(testMail)
                .authNum("OLDAUTH")
                .build();
        
        when(emailAuthRepository.findByEmail(testMail)).thenReturn(Optional.of(existingAuth));
        when(authNumberGenerator.generate()).thenReturn("NEWAUTH");

        // when
        joinEmailAuthService.sendJoinAuthEmail(sendRequestDto);

        // then
        verify(emailAuthRepository).save(existingAuth);
        assertEquals("NEWAUTH", existingAuth.getAuthNum());

        verify(emailTemplateService).sendTemplateEmail(
                eq(testMail),
                eq("[CODIN] 회원가입 인증번호입니다."),
                eq("auth-email"),
                eq("NEWAUTH")
        );
    }

    @Test
    @DisplayName("회원가입 이메일 인증 확인 - 성공")
    void checkJoinAuthEmail_성공() {
        // given
        when(emailAuthRepository.findByEmailAndAuthNum(testMail, testAuthNum))
                .thenReturn(Optional.of(mockEmailAuth));

        // when
        joinEmailAuthService.checkJoinAuthEmail(checkRequestDto);

        // then
        verify(emailAuthRepository).save(mockEmailAuth);
        assertTrue(mockEmailAuth.isVerified());
    }

    @Test
    @DisplayName("회원가입 이메일 인증 확인 - 실패 (인증번호 불일치)")
    void checkJoinAuthEmail_실패_인증번호불일치() {
        // given
        when(emailAuthRepository.findByEmailAndAuthNum(testMail, testAuthNum))
                .thenReturn(Optional.empty());

        // when & then
        EmailAuthFailException exception = assertThrows(EmailAuthFailException.class,
                () -> joinEmailAuthService.checkJoinAuthEmail(checkRequestDto));
        
        assertEquals("인증번호가 일치하지 않습니다.", exception.getMessage());
        assertEquals(testMail, exception.getEmail());
        
        verify(emailAuthRepository, never()).save(any());
    }

    @Test
    @DisplayName("회원가입 이메일 인증 확인 - 실패 (인증번호 만료)")
    void checkJoinAuthEmail_실패_인증번호만료() {
        // given
        EmailAuthEntity expiredAuth = spy(mockEmailAuth);
        when(expiredAuth.isExpired()).thenReturn(true);
        
        when(emailAuthRepository.findByEmailAndAuthNum(testMail, testAuthNum))
                .thenReturn(Optional.of(expiredAuth));

        // when & then
        EmailAuthFailException exception = assertThrows(EmailAuthFailException.class,
                () -> joinEmailAuthService.checkJoinAuthEmail(checkRequestDto));
        
        assertEquals("인증번호가 만료되었습니다.", exception.getMessage());
        assertEquals(testMail, exception.getEmail());
        
        verify(emailAuthRepository, never()).save(any());
    }

    @Test
    @DisplayName("인증번호 생성기 호출 확인")
    void authNumberGenerator_호출확인() {
        // given
        when(emailAuthRepository.findByEmail(testMail)).thenReturn(Optional.empty());
        when(authNumberGenerator.generate()).thenReturn("GENERATED");

        // when
        joinEmailAuthService.sendJoinAuthEmail(sendRequestDto);

        // then
        verify(authNumberGenerator, times(1)).generate();
    }
}