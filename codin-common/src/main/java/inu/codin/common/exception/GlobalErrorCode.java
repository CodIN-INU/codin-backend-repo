package inu.codin.common.exception;

import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

public interface GlobalErrorCode {
    HttpStatus httpStatus();
    String message();
    default Level logEvent() {
        return Level.WARN;
    }
}
