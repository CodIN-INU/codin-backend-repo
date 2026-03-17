package inu.codin.codinticketingapi.domain.user.dto;

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
    private College college;
    private Department department;

    @Builder
    public UserInfoResponse(
            String userId,
            String email,
            String name,
            Department department,
            String studentId,
            College college) {
        this._id = userId;
        this.email = email;
        this.studentId = studentId;
        this.name = name;
        this.department = department;
        this.college = college;
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
                ", studentId='" + studentId + '\'' +
                ", name='" + name + '\'' +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                ", nickname='" + nickname + '\'' +
                ", college=" + college +
                ", department=" + department +
                '}';
    }
}