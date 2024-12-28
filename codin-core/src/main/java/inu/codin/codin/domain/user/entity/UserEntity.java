package inu.codin.codin.domain.user.entity;

import inu.codin.codin.common.BaseTimeEntity;
import inu.codin.codin.common.Department;
import inu.codin.codin.domain.user.dto.request.UserUpdateRequestDto;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@Getter
public class UserEntity extends BaseTimeEntity {

    @Id @NotBlank
    private ObjectId _id;

    private String email;

    private String password;

    private String studentId;

    private String name;

    private String nickname;

    private String profileImageUrl;

    private Department department;

    private UserRole role;

    private UserStatus status;

    private boolean changePassword = false;

    @Builder
    public UserEntity(String email, String password, String studentId, String name, String nickname, String profileImageUrl, Department department, UserRole role, UserStatus status) {
        this.email = email;
        this.password = password;
        this.studentId = studentId;
        this.name = name;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.department = department;
        this.role = role;
        this.status = status;
    }

    public void updatePassword(String password){
        this.password = password;
    }

    public void changePassword(){
        this.changePassword = !this.changePassword;
    }

    public void updateUserInfo(UserUpdateRequestDto userUpdateRequestDto) {
        this.studentId = userUpdateRequestDto.getStudentId();
        this.name = userUpdateRequestDto.getName();
        this.nickname = userUpdateRequestDto.getNickname();
        this.department = userUpdateRequestDto.getDepartment();
    }

    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
