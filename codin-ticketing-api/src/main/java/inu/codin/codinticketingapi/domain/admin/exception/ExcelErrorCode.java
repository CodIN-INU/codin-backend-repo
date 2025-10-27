package inu.codin.codinticketingapi.domain.admin.exception;

import inu.codin.codinticketingapi.common.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ExcelErrorCode implements GlobalErrorCode {
    EXCEL_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "엑셀 다운로드에 실패했습니다.");

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
