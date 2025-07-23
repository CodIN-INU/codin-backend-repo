package inu.codin.codin.domain.review.exception;

import inu.codin.codin.global.common.exception.GlobalErrorCode;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ReviewErrorCode implements GlobalErrorCode {
    REVIEW_ALREADY_EXISTED(HttpStatus.CONFLICT, "이미 유저가 작성한 후기가 존재합니다."),
    REVIEW_WRONG_RATING(HttpStatus.BAD_REQUEST, "잘못된 평점입니다. 0.25 ~ 5.0 사이의 점수를 입력해주세요."),
    REVIEW_WRONG_SEMESTER(HttpStatus.BAD_REQUEST, "강의가 진행된 적 없는 학기를 선택했습니다.");

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
