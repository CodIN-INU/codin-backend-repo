package inu.codin.codin.global.common.exception;

import inu.codin.codin.domain.lecture.exception.LectureErrorCode;
import inu.codin.codin.domain.lecture.exception.LectureException;
import inu.codin.codin.domain.lecture.exception.LectureUploadException;
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
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ExceptionResponse(HttpStatus.NOT_FOUND.value(), e.getMessage()));
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
}
