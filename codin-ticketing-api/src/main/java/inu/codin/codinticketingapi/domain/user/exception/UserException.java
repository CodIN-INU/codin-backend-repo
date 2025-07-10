package inu.codin.codinticketingapi.domain.user.exception;

import inu.codin.codinticketingapi.common.exception.GlobalException;
import lombok.Getter;

@Getter
public class UserException extends GlobalException {

    private final UserErrorCode errorCode;

    public UserException(UserErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
