package inu.codin.codin.domain.user.entity;

import inu.codin.codin.common.BaseTimeEntity;
import inu.codin.codin.common.Department;
import inu.codin.codin.domain.user.dto.request.UserNicknameRequestDto;
import inu.codin.codin.common.security.dto.PortalLoginResponseDto;
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

    private String college;

    private Boolean undergraduate;

    private UserRole role;

    private UserStatus status;

    @Builder
    public UserEntity(String email, String password, String studentId, String name, String nickname, String profileImageUrl, Department department, String college, Boolean undergraduate, UserRole role, UserStatus status) {
        this.email = email;
        this.password = password;
        this.studentId = studentId;
        this.name = name;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.department = department;
        this.college = college;
        this.undergraduate = undergraduate;
        this.role = role;
        this.status = status;
    }

    public void updateNickname(UserNicknameRequestDto userNicknameRequestDto) {
        this.nickname = userNicknameRequestDto.getNickname();
    }

    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public static UserEntity of(PortalLoginResponseDto userPortalLoginResponseDto){
        return UserEntity.builder()
                .studentId(userPortalLoginResponseDto.getStudentId())
                .email(userPortalLoginResponseDto.getEmail())
                .name(userPortalLoginResponseDto.getName())
                .password(userPortalLoginResponseDto.getPassword())
                .department(userPortalLoginResponseDto.getDepartment())
                .college(userPortalLoginResponseDto.getCollege())
                .undergraduate(userPortalLoginResponseDto.getUndergraduate())
                .nickname("")
                .profileImageUrl("")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
