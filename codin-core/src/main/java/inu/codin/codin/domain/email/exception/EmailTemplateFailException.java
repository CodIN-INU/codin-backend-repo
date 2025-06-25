package inu.codin.codin.domain.email.exception;

import jdk.jfr.StackTrace;

public class EmailTemplateFailException extends RuntimeException {
    public EmailTemplateFailException(String message) {
        super(message);
    }
}
