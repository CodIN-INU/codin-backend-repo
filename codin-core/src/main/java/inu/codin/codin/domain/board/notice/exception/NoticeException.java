package inu.codin.codin.domain.board.notice.exception;

import inu.codin.codin.common.exception.GlobalException;

public class NoticeException extends GlobalException {

    public NoticeException(NoticeErrorCode errorCode) {
        super(errorCode);
    }
}
