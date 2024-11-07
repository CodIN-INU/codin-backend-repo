package inu.codin.codin.common.exception;

import lombok.Getter;

@Getter
public class EmailAuthExistException extends RuntimeException {

    private final String email;

    public EmailAuthExistException(String email) {
        super("이미 인증된 이메일입니다.");
        this.email = email;
    }

    public EmailAuthExistException(String message, String email) {
        super(message);
        this.email = email;
    }
}
