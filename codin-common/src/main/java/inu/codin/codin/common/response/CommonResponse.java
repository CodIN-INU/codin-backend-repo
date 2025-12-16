package inu.codin.codin.common.response;

import lombok.Getter;

@Getter
public class CommonResponse {
    boolean success;
    int code;
    String message;

    public CommonResponse(boolean success, int code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }
}