package inu.codin.codin.domain.email.service;

import inu.codin.codin.domain.email.dto.JoinEmailCheckRequestDto;
import inu.codin.codin.domain.email.dto.JoinEmailSendRequestDto;
import inu.codin.codin.domain.email.entity.EmailAuthEntity;
import inu.codin.codin.domain.email.exception.EmailAuthFailException;
import inu.codin.codin.domain.email.repository.EmailAuthRepository;
import inu.codin.codin.domain.email.util.AuthNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 회원가입 이메일 인증 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JoinEmailAuthService {
    private final EmailAuthRepository emailAuthRepository;
    private final EmailTemplateService emailTemplateService;
    private final AuthNumberGenerator authNumberGenerator;
    
    private static final String AUTH_EMAIL_SUBJECT = "[CODIN] 회원가입 인증번호입니다.";
    private static final String AUTH_EMAIL_TEMPLATE = "auth-email";
    
    /**
     * 회원가입용 인증 이메일을 전송합니다.
     * @param request 이메일 전송 요청 정보
     */
    @Transactional
    public void sendJoinAuthEmail(JoinEmailSendRequestDto request) {
        String email = request.getEmail();
        log.info("[sendJoinAuthEmail] email: {}", email);
        
        EmailAuthEntity emailAuthEntity = getOrCreateEmailAuth(email);
        emailAuthRepository.save(emailAuthEntity);
        
        emailTemplateService.sendTemplateEmail(
                email,
                AUTH_EMAIL_SUBJECT,
                AUTH_EMAIL_TEMPLATE,
                emailAuthEntity.getAuthNum()
        );
    }
    
    /**
     * 회원가입용 이메일 인증번호를 검증합니다.
     * @param request 인증번호 검증 요청 정보
     */
    @Transactional
    public void checkJoinAuthEmail(JoinEmailCheckRequestDto request) {
        String email = request.getEmail();
        String authNum = request.getAuthNum();

        // 이메일과 인증번호가 일치하는지 확인
        EmailAuthEntity emailAuthEntity = emailAuthRepository.findByEmailAndAuthNum(email, authNum)
                .orElseThrow(() -> new EmailAuthFailException("인증번호가 일치하지 않습니다.", email));

        // 만료 시간 검증
        if (emailAuthEntity.isExpired()) {
            throw new EmailAuthFailException("인증번호가 만료되었습니다.", email);
        }

        emailAuthEntity.verifyEmail();
        emailAuthRepository.save(emailAuthEntity);
        
        log.info("[checkJoinAuthEmail] 회원가입 이메일 인증 성공, email: {}", email);
    }
    
    /**
     * 기존 이메일 인증 정보를 조회하거나 새로 생성합니다.
     */
    private EmailAuthEntity getOrCreateEmailAuth(String email) {
        Optional<EmailAuthEntity> existingAuth = emailAuthRepository.findByEmail(email);
        
        if (existingAuth.isPresent()) {
            // 기존 인증번호 갱신
            EmailAuthEntity emailAuth = existingAuth.get();
            emailAuth.renewAuthNum(authNumberGenerator.generate());
            emailAuth.unVerifyEmail();
            return emailAuth;
        } else {
            // 새 인증 정보 생성
            return EmailAuthEntity.builder()
                    .email(email)
                    .authNum(authNumberGenerator.generate())
                    .build();
        }
    }
}
