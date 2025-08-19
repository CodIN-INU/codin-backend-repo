package inu.codin.codin.domain.board.notice.exception;

import inu.codin.codin.common.exception.GlobalErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NoticeErrorCode implements GlobalErrorCode {
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "공지사항을 찾을 수 없습니다."),
    NOTICE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "공지사항을 작성할 수 없습니다."),
    INVALID_DEPARTMENT(HttpStatus.BAD_REQUEST, "공지사항을 작성하는 유저의 학과가 유효하지 않습니다."),;

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
