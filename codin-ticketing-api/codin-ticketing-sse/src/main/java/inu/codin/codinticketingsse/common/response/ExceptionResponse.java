package inu.codin.codinticketingsse.common.response;

public class ExceptionResponse extends CommonResponse {
    public ExceptionResponse(String message, int code) {
        super(false, code, message);
    }
}
