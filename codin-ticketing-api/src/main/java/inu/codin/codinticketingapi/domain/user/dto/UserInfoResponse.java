package inu.codin.codinticketingapi.domain.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfoResponse {

    private String _id;
    private String email;
    private String name;

    public UserInfoResponse(String _id, String email, String name) {
        this._id = _id;
        this.email = email;
        this.name = name;
    }

    public String getUserId() {
        return _id;
    }

    public String getUsername() {
        return name;
    }

    @Override
    public String toString() {
        return "UserInfoResponse{" +
                "_id='" + _id + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}