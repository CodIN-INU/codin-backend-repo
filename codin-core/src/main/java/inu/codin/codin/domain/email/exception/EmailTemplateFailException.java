package inu.codin.codin.domain.email.exception;

public class EmailTemplateFailException extends RuntimeException {
    public EmailTemplateFailException(String message) {
        super(message);
    }
}
