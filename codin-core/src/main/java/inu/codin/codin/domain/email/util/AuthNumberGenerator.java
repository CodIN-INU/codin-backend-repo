package inu.codin.codin.domain.email.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 인증번호 생성 컴포넌트
 */
@Component
public class AuthNumberGenerator {
    
    /**
     * UUID 기반 8자리 랜덤 인증번호를 생성합니다.
     * @return 8자리 대문자 인증번호
     */
    public String generate() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
