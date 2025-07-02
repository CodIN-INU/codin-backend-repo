package inu.codin.codin.domain.email.exception;

public class EmailPasswordResetFailException extends RuntimeException {
    public EmailPasswordResetFailException(String message) {
        super(message);
    }
}
