package inu.codin.codin.domain.email.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.domain.email.dto.JoinEmailSendRequestDto;
import inu.codin.codin.domain.email.entity.EmailAuthEntity;
import inu.codin.codin.domain.email.repository.EmailAuthRepository;
import inu.codin.codin.domain.email.util.AuthNumberGenerator;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.repository.UserRepository;
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

    @BeforeEach
    void setUp() {
        mockUser = mock(UserEntity.class);
        requestDto = JoinEmailSendRequestDto.builder()
                .email("test@inu.ac.kr")
                .build();
        existingEmailAuth = EmailAuthEntity.builder()
                .email("test@inu.ac.kr")
                .authNum("OLDAUTH1")
                .build();
    }

    @Test
    @DisplayName("비밀번호 재설정 이메일 전송 - 성공 (신규 사용자)")
    void sendPasswordResetEmail_성공_신규사용자() {
        // given
        when(userRepository.findByEmail("test@inu.ac.kr")).thenReturn(Optional.of(mockUser));
        when(emailAuthRepository.findByEmail("test@inu.ac.kr")).thenReturn(Optional.empty());
        when(authNumberGenerator.generate()).thenReturn("NEWAUTH1");

        // when
        passwordResetEmailService.sendPasswordResetEmail(requestDto);

        // then
        ArgumentCaptor<EmailAuthEntity> emailAuthCaptor = ArgumentCaptor.forClass(EmailAuthEntity.class);
        verify(emailAuthRepository).save(emailAuthCaptor.capture());
        
        EmailAuthEntity savedAuth = emailAuthCaptor.getValue();
        assertEquals("test@inu.ac.kr", savedAuth.getEmail());
        assertEquals("NEWAUTH1", savedAuth.getAuthNum());
        assertFalse(savedAuth.isVerified());

        verify(emailTemplateService).sendTemplateEmail(
                eq("test@inu.ac.kr"),
                eq("[CODIN] 비밀번호 재설정 링크입니다."),
                eq("password-email"),
                eq("NEWAUTH1")
        );
    }

    @Test
    @DisplayName("비밀번호 재설정 이메일 전송 - 성공 (기존 인증 정보 갱신)")
    void sendPasswordResetEmail_성공_기존인증정보갱신() {
        // given
        when(userRepository.findByEmail("test@inu.ac.kr")).thenReturn(Optional.of(mockUser));
        when(emailAuthRepository.findByEmail("test@inu.ac.kr")).thenReturn(Optional.of(existingEmailAuth));
        when(authNumberGenerator.generate()).thenReturn("NEWAUTH2");

        // when
        passwordResetEmailService.sendPasswordResetEmail(requestDto);

        // then
        verify(emailAuthRepository).save(existingEmailAuth);
        assertEquals("NEWAUTH2", existingEmailAuth.getAuthNum());
        assertFalse(existingEmailAuth.isVerified()); // 인증 상태 초기화 확인

        verify(emailTemplateService).sendTemplateEmail(
                eq("test@inu.ac.kr"),
                eq("[CODIN] 비밀번호 재설정 링크입니다."),
                eq("password-email"),
                eq("NEWAUTH2")
        );
    }

    @Test
    @DisplayName("비밀번호 재설정 이메일 전송 - 실패 (존재하지 않는 사용자)")
    void sendPasswordResetEmail_실패_존재하지않는사용자() {
        // given
        when(userRepository.findByEmail("test@inu.ac.kr")).thenReturn(Optional.empty());

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
        when(userRepository.findByEmail("test@inu.ac.kr")).thenReturn(Optional.of(mockUser));
        when(emailAuthRepository.findByEmail("test@inu.ac.kr")).thenReturn(Optional.empty());
        when(authNumberGenerator.generate()).thenReturn("GENERATED");

        // when
        passwordResetEmailService.sendPasswordResetEmail(requestDto);

        // then
        verify(authNumberGenerator, times(1)).generate();
    }
}