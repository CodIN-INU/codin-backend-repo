package inu.codin.codin.domain.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EmailSendService {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    // 이메일 전송 로직
    // +템플릿 엔진으로 이메일 전송 추가 필요!!
    @Async
    public void sendAuthEmail(String email, String authNum) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(email);
            helper.setSubject("[CODIN] 회원가입 인증번호입니다.");
            helper.setText("인증번호 : " + authNum, true);
            javaMailSender.send(message);
            log.info("[sendAuthEmail] 인증 이메일 전송 성공, email : {}", email);
        } catch (MessagingException e) {
            log.error("[sendAuthEmail] 인증 이메일 전송 실패, email : {}", email);
            throw new RuntimeException(e);
        }
    }
}
