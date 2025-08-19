package inu.codin.codin.domain.board.notice.exception;

import inu.codin.codin.common.exception.GlobalException;
import lombok.Getter;

@Getter
public class NoticeException extends GlobalException {
    private final NoticeErrorCode errorCode;
    public NoticeException(NoticeErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
