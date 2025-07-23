package inu.codin.codinticketingapi.domain.user.dto;

import inu.codin.codinticketingapi.domain.ticketing.entity.Department;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfoResponse {

    private String _id;
    private String email;
    private String name;
    private Department department;
    private String studentId;

    public UserInfoResponse(String _id, String email, String name, Department department, String studentId) {
        this._id = _id;
        this.email = email;
        this.name = name;
        this.department = department;
        this.studentId = studentId;
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
                ", department=" + department +
                ", studentId='" + studentId + '\'' +
                '}';
    }
}