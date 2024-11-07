package inu.codin.codin.domain.email.service;

import inu.codin.codin.common.exception.EmailAuthExistException;
import inu.codin.codin.domain.email.dto.JoinEmailCheckRequestDto;
import inu.codin.codin.domain.email.dto.JoinEmailSendRequestDto;
import inu.codin.codin.domain.email.entity.EmailAuthEntity;
import inu.codin.codin.domain.email.repository.EmailAuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailAuthService {

    private final EmailAuthRepository emailAuthRepository;

    // + 비동기 방식 고려
    public void sendAuthEmail(JoinEmailSendRequestDto joinEmailSendRequestDto) {

        String email = joinEmailSendRequestDto.getEmail();
        log.info("[sendAuthEmail] email : {}", email);

        if (emailAuthRepository.existsByEmail(email))
            throw new EmailAuthExistException(email);

        EmailAuthEntity emailAuthEntity = EmailAuthEntity.builder()
                .email(email)
                .authNum(generateAuthNum())
                .build();

        emailAuthRepository.save(emailAuthEntity);

        // + 이메일 전송 로직 추가
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
                .orElseThrow(() -> new IllegalArgumentException("인증번호가 일치하지 않습니다."));

        log.info("[checkAuthNum] Email Auth SUCCESS!!, email : {}", email);
        emailAuthEntity.verifyEmail();
    }
}