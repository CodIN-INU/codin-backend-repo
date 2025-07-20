package inu.codin.codinlectureapi.global.response;

public class ExceptionResponse extends CommonResponse{
    public ExceptionResponse(int code, String message) {
        super(false, code, message);
    }
}
