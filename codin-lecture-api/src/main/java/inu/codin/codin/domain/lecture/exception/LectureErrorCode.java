package inu.codin.codin.domain.lecture.exception;

import inu.codin.common.exception.GlobalErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum LectureErrorCode implements GlobalErrorCode {

    LECTURE_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "강의 내역(LECTURE) 업로드에 실패했습니다.", Level.ERROR),
    LECTURE_NOT_FOUND(HttpStatus.NOT_FOUND, "강의 내역을 찾을 수 없습니다.", Level.WARN),

    LECTURE_ROOM_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "강의실 내역(ROOM) 업로드에 실패했습니다.", Level.ERROR),
    LECTURE_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "강의실 내역을 찾을 수 없습니다.", Level.WARN),

    FILE_READ_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "파일을 읽을 수 없습니다.", Level.WARN),
    DEPARTMENT_WRONG_INPUT(HttpStatus.BAD_REQUEST, "학과명을 잘못 입력했습니다.", Level.INFO),
    AI_SUMMARY_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI 요약 생성 중 오류 발생했습니다.", Level.WARN),

    CONVERT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Converter 오류가 발생했습니다.", Level.ERROR);

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
