package inu.codin.codin.domain.user.exception;

import org.springframework.security.authentication.AccountStatusException;

public class UserDisabledException extends AccountStatusException {
    public UserDisabledException(String message) {
        super(message);
    }

    public UserDisabledException(String message, Throwable cause) {
        super(message, cause);
    }
}
