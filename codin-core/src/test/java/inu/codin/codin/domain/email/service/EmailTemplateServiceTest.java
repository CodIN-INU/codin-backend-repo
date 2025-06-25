package inu.codin.codin.domain.email.service;

import inu.codin.codin.domain.email.exception.EmailTemplateFailException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailTemplateServiceTest {

    @InjectMocks
    EmailTemplateService emailTemplateService;

    @Mock
    JavaMailSender javaMailSender;
    @Mock
    SpringTemplateEngine templateEngine;
    @Mock
    MimeMessage mimeMessage;

    private final String testEmail = "test@inu.ac.kr";
    private final String testSubject = "테스트 제목";
    private final String testTemplate = "test-template";
    private final String testAuthNum = "AUTH123";
    private final String testHtmlContent = "<html><body>인증번호: " + testAuthNum + "</body></html>";

    @BeforeEach
    void setUp() {
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    @DisplayName("템플릿 이메일 전송 - 성공")
    void sendTemplateEmail_성공() {
        // given
        when(templateEngine.process(eq(testTemplate), any(Context.class)))
                .thenReturn(testHtmlContent);

        // when
        emailTemplateService.sendTemplateEmail(testEmail, testSubject, testTemplate, testAuthNum);

        // then
        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(mimeMessage);
        verify(templateEngine).process(eq(testTemplate), any(Context.class));
    }

    @Test
    @DisplayName("템플릿 이메일 전송 - 템플릿 엔진 컨텍스트 확인")
    void sendTemplateEmail_템플릿엔진컨텍스트확인() {
        // given
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        when(templateEngine.process(eq(testTemplate), contextCaptor.capture()))
                .thenReturn(testHtmlContent);

        // when
        emailTemplateService.sendTemplateEmail(testEmail, testSubject, testTemplate, testAuthNum);

        // then
        Context capturedContext = contextCaptor.getValue();
        assertNotNull(capturedContext);
        verify(templateEngine).process(eq(testTemplate), any(Context.class));
    }

    @Test
    @DisplayName("템플릿 이메일 전송 - MimeMessage 생성 실패")
    void sendTemplateEmail_MimeMessage생성실패() {
        // given
        when(javaMailSender.createMimeMessage()).thenThrow(new EmailTemplateFailException("MimeMessage 생성 실패"));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> emailTemplateService.sendTemplateEmail(testEmail, testSubject, testTemplate, testAuthNum));
        
        assertEquals("MimeMessage 생성 실패", exception.getMessage());
        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("템플릿 이메일 전송 - 템플릿 처리 실패")
    void sendTemplateEmail_템플릿처리실패() {
        // given
        when(templateEngine.process(eq(testTemplate), any(Context.class)))
                .thenThrow(new EmailTemplateFailException("템플릿 처리 실패"));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> emailTemplateService.sendTemplateEmail(testEmail, testSubject, testTemplate, testAuthNum));
        
        assertTrue(exception.getMessage().contains("템플릿 처리 실패"));
        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("템플릿 이메일 전송 - 이메일 전송 실패")
    void sendTemplateEmail_이메일전송실패() {
        // given
        when(templateEngine.process(eq(testTemplate), any(Context.class)))
                .thenReturn(testHtmlContent);
        doThrow(new EmailTemplateFailException("이메일 전송 실패"))
                .when(javaMailSender).send(mimeMessage);

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> emailTemplateService.sendTemplateEmail(testEmail, testSubject, testTemplate, testAuthNum));
        
        assertTrue(exception.getMessage().contains("이메일 전송 실패"));
    }

    @Test
    @DisplayName("템플릿 이메일 전송 - 파라미터 검증")
    void sendTemplateEmail_파라미터검증() {
        // given
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn(testHtmlContent);

        // when
        emailTemplateService.sendTemplateEmail(testEmail, testSubject, testTemplate, testAuthNum);

        // then
        verify(templateEngine).process(eq(testTemplate), any(Context.class));
        verify(javaMailSender).send(mimeMessage);
    }
}