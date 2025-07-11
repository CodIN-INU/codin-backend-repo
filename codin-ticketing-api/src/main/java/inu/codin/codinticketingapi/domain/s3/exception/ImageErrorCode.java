package inu.codin.codinticketingapi.domain.s3.exception;

import inu.codin.codinticketingapi.common.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ImageErrorCode implements GlobalErrorCode {

    FILE_SIZE(HttpStatus.BAD_REQUEST, "파일 크기가 5MB를 초과할 수 없습니다."),
    REMOVE_FAILED(HttpStatus.BAD_REQUEST, "파일 삭제에 실패했습니다."),
    MAX_FILE_COUNT(HttpStatus.BAD_REQUEST, "이미지 파일 개수는 최대 10개까지 업로드 가능합니다."),
    BAD_IMAGE_TYPE(HttpStatus.BAD_REQUEST, "유효한 이미지 파일(jpg, jpeg, png, gif)이 아닙니다."),
    IMAGE_NOT_EXIST(HttpStatus.BAD_REQUEST, "삭제할 이미지가 존재하지 않습니다."),
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패했습니다"),
    BAD_BUCKET_NAME(HttpStatus.INTERNAL_SERVER_ERROR, "S3 버킷 이름이 설정되지 않았습니다.");

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
