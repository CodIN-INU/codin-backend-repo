package inu.codin.codin.global.common.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SingleResponse<T> extends CommonResponse {
    T data;

    @Builder
    public SingleResponse(int code, String message, T data) {
        super(true, code, message);
        this.data = data;
    }
}
