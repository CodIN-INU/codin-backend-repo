package inu.codin.codin.domain.calendar.exception;

import inu.codin.codin.common.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum CalendarErrorCode implements GlobalErrorCode {

    CALENDAR_EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "달력을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public HttpStatus httpStatus() {
        return httpStatus;
    }

    @Override
    public String message() {
        return message;
    }
}

