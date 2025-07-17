package inu.codin.codin.domain.post.schedular.exception;

import inu.codin.codin.common.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum SchedulerErrorCode implements GlobalErrorCode {
    DUPLICATE_ANONYMOUS_STATE(HttpStatus.CONFLICT, "현재 익명 상태와 동일한 상태로 변경할 수 없습니다."),
    DUPLICATE_POST_STATUS(HttpStatus.CONFLICT, "현재 게시글 상태와 동일한 상태로 변경할 수 없습니다."),
    SCHEDULER_INTERRUPT_ERROR(HttpStatus.BAD_REQUEST,"스케줄러 실행중 에러가 발생했습니다");

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
