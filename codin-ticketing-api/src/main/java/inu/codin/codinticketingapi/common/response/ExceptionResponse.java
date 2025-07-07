package inu.codin.codinticketingapi.common.response;

public class ExceptionResponse extends CommonResponse{
    public ExceptionResponse(int code, String message) {
        super(false, code, message);
    }
}
