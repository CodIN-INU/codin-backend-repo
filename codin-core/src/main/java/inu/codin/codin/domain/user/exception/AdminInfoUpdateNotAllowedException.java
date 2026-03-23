package inu.codin.codin.domain.user.exception;

public class AdminInfoUpdateNotAllowedException extends RuntimeException {
    public AdminInfoUpdateNotAllowedException(String message) {
        super(message);
    }
}
