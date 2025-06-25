package inu.codin.codin.domain.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

/**
 * 이메일 템플릿 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTemplateService {
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
    
    /**
     * 템플릿을 사용해 이메일을 전송합니다.
     * @param email 수신자 이메일
     * @param subject 이메일 제목
     * @param templateName 템플릿 이름
     * @param authNum 인증번호
     */
    @Async
    public void sendTemplateEmail(String email, String subject, String templateName, String authNum) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            
            // 템플릿 컨텍스트 설정
            Context context = new Context();
            context.setVariable("authNum", authNum);
            
            // HTML 내용 생성
            String htmlContent = templateEngine.process(templateName, context);
            
            // 이메일 설정
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            // 이메일 전송
            javaMailSender.send(message);
            log.info("[sendTemplateEmail] 이메일 전송 성공, email: {}, template: {}", email, templateName);
        } catch (MessagingException e) {
            log.error("[sendTemplateEmail] 이메일 전송 실패 (MessagingException), email: {}, template: {}", email, templateName, e);
            throw new RuntimeException("이메일 전송에 실패했습니다.", e);
        } catch (RuntimeException e) {
            log.error("[sendTemplateEmail] 이메일 전송 실패 (RuntimeException), email: {}, template: {}", email, templateName, e);
            throw new RuntimeException("이메일 전송 중 알 수 없는 오류가 발생했습니다.", e);
        }
    }
}
