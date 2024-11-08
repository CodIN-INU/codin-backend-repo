package inu.codin.codin.domain.email.service;

import inu.codin.codin.common.exception.EmailAuthFailException;
import inu.codin.codin.domain.email.dto.JoinEmailCheckRequestDto;
import inu.codin.codin.domain.email.dto.JoinEmailSendRequestDto;
import inu.codin.codin.domain.email.entity.EmailAuthEntity;
import inu.codin.codin.domain.email.repository.EmailAuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailAuthService {

    private final EmailAuthRepository emailAuthRepository;
    private final EmailSendService emailSendService;

    // + 비동기 방식 고려
    public void sendAuthEmail(JoinEmailSendRequestDto joinEmailSendRequestDto) {

        String email = joinEmailSendRequestDto.getEmail();
        log.info("[sendAuthEmail] email : {}", email);

        Optional<EmailAuthEntity> emailAuth = emailAuthRepository.findByEmail(email);
        EmailAuthEntity emailAuthEntity;

        // 재인증 로직
        if (emailAuth.isPresent()) {
            emailAuthEntity = emailAuth.get();

            if (emailAuthEntity.isVerified()) {
                throw new EmailAuthFailException("이미 인증된 이메일입니다.", email);
            }

            emailAuthEntity.changeAuthNum(generateAuthNum());
        }
        else {
            // 인증 생성 로직
            emailAuthEntity = EmailAuthEntity.builder()
                    .email(email)
                    .authNum(generateAuthNum())
                    .build();
        }
        emailAuthRepository.save(emailAuthEntity);

        // 이메일 전송 로직
        emailSendService.sendAuthEmail(email, emailAuthEntity.getAuthNum());
    }

    private String generateAuthNum() {
        // 8자리 인증번호 생성
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public void checkAuthNum(JoinEmailCheckRequestDto joinEmailCheckRequestDto) {

        String email = joinEmailCheckRequestDto.getEmail();
        String authNum = joinEmailCheckRequestDto.getAuthNum();
        log.info("[checkAuthNum] email : {}, authNum : {}", email, authNum);

        // 이메일과 인증번호가 일치하는지 확인
        EmailAuthEntity emailAuthEntity = emailAuthRepository.findByEmailAndAuthNum(email, authNum)
                .orElseThrow(() -> new EmailAuthFailException("인증번호가 일치하지 않습니다.", email));

        // 10분 이내에 인증하지 않을 시에 인증번호 만료
        if (emailAuthEntity.isExpired()) {
            throw new EmailAuthFailException("인증번호가 만료되었습니다.", email);
        }

        log.info("[checkAuthNum] Email Auth SUCCESS!!, email : {}", email);
        emailAuthEntity.verifyEmail();
    }
}