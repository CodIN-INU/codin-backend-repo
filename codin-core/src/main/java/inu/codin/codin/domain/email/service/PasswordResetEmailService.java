package inu.codin.codin.domain.email.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.domain.email.dto.JoinEmailSendRequestDto;
import inu.codin.codin.domain.email.entity.EmailAuthEntity;
import inu.codin.codin.domain.email.exception.EmailPasswordResetFailException;
import inu.codin.codin.domain.email.repository.EmailAuthRepository;
import inu.codin.codin.domain.email.util.AuthNumberGenerator;
import inu.codin.codin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 비밀번호 재설정 메일링 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetEmailService {
    private final EmailAuthRepository emailAuthRepository;
    private final EmailTemplateService emailTemplateService;
    private final UserRepository userRepository;
    private final AuthNumberGenerator authNumberGenerator;
    
    private static final String PASSWORD_EMAIL_SUBJECT = "[CODIN] 비밀번호 재설정 링크입니다.";
    private static final String PASSWORD_EMAIL_TEMPLATE = "password-email";
    
    /**
     * 비밀번호 재설정용 이메일을 전송합니다.
     * @param request 이메일 전송 요청 정보
     */
    @Transactional
    public void sendPasswordResetEmail(JoinEmailSendRequestDto request) {
        String email = request.getEmail();
        log.info("[sendPasswordResetEmail] email: {}", email);
        
        // 사용자 존재 여부 확인
        userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("회원가입을 먼저 진행해주세요."));
        
        EmailAuthEntity emailAuthEntity = getOrCreatePasswordResetAuth(email);
        emailAuthRepository.save(emailAuthEntity);
        
        emailTemplateService.sendTemplateEmail(
                email,
                PASSWORD_EMAIL_SUBJECT,
                PASSWORD_EMAIL_TEMPLATE,
                emailAuthEntity.getAuthNum()
        );
    }
    
    /**
     * 비밀번호 재설정용 인증 정보를 조회하거나 새로 생성합니다.
     */
    private EmailAuthEntity getOrCreatePasswordResetAuth(String email) {
        Optional<EmailAuthEntity> existingAuth = emailAuthRepository.findByEmail(email);
        
        if (existingAuth.isPresent()) {
            // 기존 인증번호 갱신 및 인증 상태 초기화
            EmailAuthEntity emailAuth = existingAuth.get();
            emailAuth.renewAuthNum(authNumberGenerator.generate());
            emailAuth.unVerifyEmail();
            return emailAuth;
        } else {
            log.error("[getOrCreatePasswordResetAuth] 등록되지 않은 사용자에 대한 비밀번호 초기화 요청, email: {}", email);
            throw new EmailPasswordResetFailException("등록되지 않은 사용자 이메일입니다.");
        }
    }
}
