package inu.codin.codin.domain.calendar.exception;

import inu.codin.codin.common.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum CalendarErrorCode implements GlobalErrorCode {

    CALENDAR_EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "캘린더 이벤트를 찾을 수 없습니다."),
    DATE_CANNOT_NULL(HttpStatus.BAD_REQUEST, "날짜를 입력해야 합니다."),
    DATE_FORMAT_ERROR(HttpStatus.BAD_REQUEST, "날짜 형식이 잘못되었습니다.");

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

