package inu.codin.codin.domain.email.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.domain.email.dto.JoinEmailSendRequestDto;
import inu.codin.codin.domain.email.entity.EmailAuthEntity;
import inu.codin.codin.domain.email.exception.EmailPasswordResetFailException;
import inu.codin.codin.domain.email.repository.EmailAuthRepository;
import inu.codin.codin.domain.email.util.AuthNumberGenerator;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetEmailServiceTest {

    @InjectMocks
    PasswordResetEmailService passwordResetEmailService;

    @Mock
    EmailAuthRepository emailAuthRepository;
    @Mock
    EmailTemplateService emailTemplateService;
    @Mock
    UserRepository userRepository;
    @Mock
    AuthNumberGenerator authNumberGenerator;

    private JoinEmailSendRequestDto requestDto;
    private UserEntity mockUser;
    private EmailAuthEntity existingEmailAuth;

    private final String testMail = "test@inu.ac.kr";
    private final String testAuthNum = "AUTH123";

    @BeforeEach
    void setUp() {
        mockUser = mock(UserEntity.class);
        requestDto = JoinEmailSendRequestDto.builder()
                .email(testMail)
                .build();
        existingEmailAuth = EmailAuthEntity.builder()
                .email(testMail)
                .authNum(testAuthNum)
                .build();
    }

    @Test
    @DisplayName("비밀번호 재설정 이메일 전송 - 실패 (등록되지 않은 사용자)")
    void sendPasswordResetEmail_성공_신규사용자() {
        // given
        when(userRepository.findByEmail(testMail)).thenReturn(Optional.of(mockUser));
        when(emailAuthRepository.findByEmail(testMail)).thenReturn(Optional.empty());

        // when & then
        EmailPasswordResetFailException exception = assertThrows(EmailPasswordResetFailException.class,
                () -> passwordResetEmailService.sendPasswordResetEmail(requestDto));

        verify(emailAuthRepository, never()).save(any());
        verify(emailTemplateService, never()).sendTemplateEmail(anyString(), anyString(), anyString(), anyString());
        verify(authNumberGenerator, never()).generate();
    }

    @Test
    @DisplayName("비밀번호 재설정 이메일 전송 - 성공 (기존 인증 정보 갱신)")
    void sendPasswordResetEmail_성공_기존인증정보갱신() {
        // given
        when(userRepository.findByEmail(testMail)).thenReturn(Optional.of(mockUser));
        when(emailAuthRepository.findByEmail(testMail)).thenReturn(Optional.of(existingEmailAuth));
        when(authNumberGenerator.generate()).thenReturn("NEWAUTH");

        // when
        passwordResetEmailService.sendPasswordResetEmail(requestDto);

        // then
        verify(emailAuthRepository).save(existingEmailAuth);
        assertEquals("NEWAUTH", existingEmailAuth.getAuthNum());
        assertFalse(existingEmailAuth.isVerified()); // 인증 상태 초기화 확인

        verify(emailTemplateService).sendTemplateEmail(
                eq(testMail),
                eq("[CODIN] 비밀번호 재설정 링크입니다."),
                eq("password-email"),
                eq("NEWAUTH")
        );
    }

    @Test
    @DisplayName("비밀번호 재설정 이메일 전송 - 실패 (존재하지 않는 사용자)")
    void sendPasswordResetEmail_실패_존재하지않는사용자() {
        // given
        when(userRepository.findByEmail(testMail)).thenReturn(Optional.empty());

        // when & then
        NotFoundException exception = assertThrows(NotFoundException.class, 
                () -> passwordResetEmailService.sendPasswordResetEmail(requestDto));
        
        assertEquals("회원가입을 먼저 진행해주세요.", exception.getMessage());
        
        verify(emailAuthRepository, never()).save(any());
        verify(emailTemplateService, never()).sendTemplateEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("비밀번호 재설정 이메일 전송 - 인증번호 생성기 호출 확인")
    void sendPasswordResetEmail_인증번호생성기호출확인() {
        // given
        when(userRepository.findByEmail(testMail)).thenReturn(Optional.of(mockUser));
        when(emailAuthRepository.findByEmail(testMail)).thenReturn(Optional.of(existingEmailAuth));
        when(authNumberGenerator.generate()).thenReturn(testAuthNum);

        // when
        passwordResetEmailService.sendPasswordResetEmail(requestDto);

        // then
        verify(authNumberGenerator, times(1)).generate();
    }
}