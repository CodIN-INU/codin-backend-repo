package inu.codin.codin.domain.review.exception;

import inu.codin.common.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ReviewErrorCode implements GlobalErrorCode {
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 후기를 찾을 수 없습니다.", Level.WARN),
    REVIEW_ALREADY_EXISTED(HttpStatus.CONFLICT, "이미 유저가 작성한 후기가 존재합니다.", Level.WARN),
    REVIEW_WRONG_RATING(HttpStatus.BAD_REQUEST, "잘못된 평점입니다. 0.25 ~ 5.0 사이의 점수를 입력해주세요.", Level.WARN),
    REVIEW_WRONG_SEMESTER(HttpStatus.BAD_REQUEST, "강의가 진행된 적 없는 학기를 선택했습니다.", Level.WARN);

    private final HttpStatus httpStatus;
    private final String message;
    private final Level logLevel;


    @Override
    public HttpStatus httpStatus() {
        return httpStatus;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public Level logEvent() {
        return logLevel;
    }
}
