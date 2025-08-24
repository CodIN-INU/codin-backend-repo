package inu.codin.codin.domain.calendar.exception;

import inu.codin.codin.common.exception.GlobalException;
import lombok.Getter;

@Getter
public class CalendarException extends GlobalException {

    private final CalendarErrorCode calendarErrorCode;

    public CalendarException(CalendarErrorCode calendarErrorCode) {
        super(calendarErrorCode);
        this.calendarErrorCode = calendarErrorCode;
    }
}
