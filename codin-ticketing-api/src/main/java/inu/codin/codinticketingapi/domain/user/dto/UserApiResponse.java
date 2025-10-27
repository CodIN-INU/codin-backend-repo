package inu.codin.codinticketingapi.domain.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserApiResponse {

    private boolean success;
    private int code;
    private String message;
    private UserInfoResponse data;

    public UserApiResponse(boolean success, int code, String message, UserInfoResponse data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    @Override
    public String toString() {
        return "UserApiResponse{" +
                "success=" + success +
                ", code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data != null ? data.toString() : "null" +
                '}';
    }
}
