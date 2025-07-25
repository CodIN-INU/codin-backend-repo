package inu.codin.codin.global.common.response;

public class ExceptionResponse extends CommonResponse{
    public ExceptionResponse(int code, String message) {
        super(false, code, message);
    }
}
