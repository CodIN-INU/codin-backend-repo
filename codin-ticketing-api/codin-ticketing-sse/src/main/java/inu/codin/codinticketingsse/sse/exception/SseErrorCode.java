package inu.codin.codinticketingsse.sse.exception;

import inu.codin.codinticketingsse.common.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum SseErrorCode implements GlobalErrorCode {

    SSE_SEND_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SSE를 전송할 수 없습니다.");

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

