package inu.codin.lecture.domain.user.dto;

import inu.codin.common.entity.College;
import inu.codin.common.entity.Department;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfoResponse {

    private String _id;
    private String email;
    private String studentId;
    private String name;
    private String nickname;
    private String profileImageUrl;
    private Department department;
    private College college;

    @Builder
    public UserInfoResponse(String userId, String email, String name, Department department, College college, String studentId) {
        this._id = userId;
        this.email = email;
        this.name = name;
        this.department = department;
        this.studentId = studentId;
        this.college = college;
    }

    public String getUserId() {
        return _id;
    }

    @Override
    public String toString() {
        return "UserInfoResponse{" +
                "_id='" + _id + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", department=" + department +
                ", studentId='" + studentId + '\'' +
                ", college='" + college + '\'' +
                '}';
    }
}