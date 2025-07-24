package inu.codin.codinticketingapi.common.response;

public class ExceptionResponse extends CommonResponse{
    public ExceptionResponse(String message, int code) {
        super(false, code, message);
    }
}
