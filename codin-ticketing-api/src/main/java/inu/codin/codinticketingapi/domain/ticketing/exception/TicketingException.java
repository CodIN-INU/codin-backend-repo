package inu.codin.codinticketingapi.domain.ticketing.exception;

import inu.codin.codinticketingapi.common.exception.GlobalException;
import lombok.Getter;

@Getter
public class TicketingException extends GlobalException {

    private final TicketingErrorCode errorCode;

    public TicketingException(TicketingErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
