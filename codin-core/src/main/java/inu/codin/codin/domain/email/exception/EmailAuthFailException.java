package inu.codin.codin.domain.email.exception;

import lombok.Getter;

@Getter
public class EmailAuthFailException extends RuntimeException {

    private final String email;

    public EmailAuthFailException(String email) {
        super("이미 인증된 이메일입니다.");
        this.email = email;
    }

    public EmailAuthFailException(String message, String email) {
        super(message);
        this.email = email;
    }
}
