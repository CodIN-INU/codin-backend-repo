package inu.codin.codin.domain.post.domain.poll.exception;

import inu.codin.codin.common.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum PollErrorCode implements GlobalErrorCode {
    POLL_NOT_FOUND(HttpStatus.NOT_FOUND, "투표 정보를 찾을 수 없습니다."),
    POLL_FINISHED(HttpStatus.BAD_REQUEST, "이미 종료된 투표입니다."),
    POLL_DUPLICATED(HttpStatus.CONFLICT, "이미 투표하셨습니다."),
    MULTIPLE_CHOICE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "복수 선택이 허용되지 않은 투표입니다."),
    INVALID_OPTION(HttpStatus.BAD_REQUEST, "잘못된 선택지입니다."),
    POLL_VOTE_STATE_CONFLICT(HttpStatus.CONFLICT, "투표 증감 실패."),
    POLL_VOTE_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자의 일치하는 투표 내역이 없습니다."),
    DUPLICATE_SELECTION(HttpStatus.BAD_REQUEST, "중복 선택입니다.");
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