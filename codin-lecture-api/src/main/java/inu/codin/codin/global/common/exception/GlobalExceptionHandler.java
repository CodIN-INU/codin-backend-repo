package inu.codin.codin.global.common.exception;

import inu.codin.codin.domain.lecture.exception.LectureErrorCode;
import inu.codin.codin.domain.lecture.exception.LectureException;
import inu.codin.codin.domain.lecture.exception.LectureUploadException;
import inu.codin.codin.domain.like.exception.LikeErrorCode;
import inu.codin.codin.domain.like.exception.LikeException;
import inu.codin.codin.domain.review.exception.ReviewErrorCode;
import inu.codin.codin.domain.review.exception.ReviewException;
import inu.codin.codin.global.common.response.ExceptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ExceptionResponse> handleException(Exception e) {
        StackTraceElement[] stack = e.getStackTrace();

        if (stack != null && stack.length > 0) {
            StackTraceElement stackTraceElement = stack[0];

            log.error("[Exception] 발생 위치: {}.{}({}:{}) | 원인: {} - {}",
                    stackTraceElement.getClassName(),
                    stackTraceElement.getMethodName(),
                    stackTraceElement.getFileName(),
                    stackTraceElement.getLineNumber(),
                    e.getClass().getSimpleName(),
                    e.getMessage(),
                    e);
        } else {
            log.error("[Exception] 원인: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류가 발생했습니다."));
    }

    @ExceptionHandler(LectureException.class)
    protected ResponseEntity<ExceptionResponse> handleLectureException(LectureException e) {
        LectureErrorCode lectureErrorCode = e.getErrorCode();
        return ResponseEntity.status(lectureErrorCode.httpStatus())
                .body(new ExceptionResponse(lectureErrorCode.httpStatus().value(), lectureErrorCode.getMessage()));
    }

    @ExceptionHandler(LectureUploadException.class)
    protected ResponseEntity<ExceptionResponse> handleLectureUploadException(LectureUploadException e) {
        LectureErrorCode lectureErrorCode = e.getErrorCode();
        String errorMessage = lectureErrorCode.getMessage();
        if (e.getExitCode() != null)
            errorMessage += " [exitCode] : " + e.getExitCode();
        else if (e.getErrorMessage() != null)
            errorMessage += " [errorMessage] : " + e.getErrorMessage();
        return ResponseEntity.status(lectureErrorCode.httpStatus())
                .body(new ExceptionResponse(lectureErrorCode.httpStatus().value(), errorMessage));
    }

    @ExceptionHandler(ReviewException.class)
    protected ResponseEntity<ExceptionResponse> handleReviewException(ReviewException e) {
        ReviewErrorCode reviewErrorCode = e.getErrorCode();
        return ResponseEntity.status(reviewErrorCode.httpStatus())
                .body(new ExceptionResponse(reviewErrorCode.httpStatus().value(), e.getMessage()));
    }

    @ExceptionHandler(LikeException.class)
    protected ResponseEntity<ExceptionResponse> handleLikeException(LikeException e) {
        LikeErrorCode likeErrorCode = e.getErrorCode();
        String errorMessage = e.getMessage() + " : " + e.getErrorMessage();
        return ResponseEntity.status(likeErrorCode.httpStatus())
                .body(new ExceptionResponse(likeErrorCode.httpStatus().value(), errorMessage));
    }
}
