package inu.codin.codinticketingapi.domain.admin.exception;

import inu.codin.codinticketingapi.common.exception.GlobalException;
import lombok.Getter;

@Getter
public class ExcelException extends GlobalException {
    private final ExcelErrorCode errorCode;

    public ExcelException(ExcelErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
