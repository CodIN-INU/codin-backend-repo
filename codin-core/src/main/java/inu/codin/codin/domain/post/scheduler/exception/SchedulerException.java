package inu.codin.codin.domain.post.scheduler.exception;

import inu.codin.codin.common.exception.GlobalException;
import lombok.Getter;

@Getter
public class SchedulerException extends GlobalException {
    private final SchedulerErrorCode errorCode;
    public SchedulerException(SchedulerErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
