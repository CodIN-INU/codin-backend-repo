package inu.codin.codinticketingapi.domain.s3.exception;

import inu.codin.codinticketingapi.common.exception.GlobalException;
import lombok.Getter;

@Getter
public class ImageException extends GlobalException {

    private final ImageErrorCode errorCode;

    public ImageException(ImageErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
