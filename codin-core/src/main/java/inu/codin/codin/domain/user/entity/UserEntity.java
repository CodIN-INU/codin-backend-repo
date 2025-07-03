package inu.codin.codin.domain.user.entity;

import inu.codin.codin.common.dto.BaseTimeEntity;
import inu.codin.codin.common.dto.Department;
import inu.codin.codin.common.security.dto.PortalLoginResponseDto;
import inu.codin.codin.domain.notification.entity.NotificationPreference;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

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

    private LocalDateTime totalSuspensionEndDate; //정지 게시물이 늘어날수록 정지 종료일이 중첩

    private NotificationPreference notificationPreference = new NotificationPreference();

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

    public void updateNickname(String nickname) {
        this.nickname = nickname;
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

    public void suspendUser() {
        this.status = UserStatus.SUSPENDED;
    }

    public void activateUser() {
        if ( this.status == UserStatus.SUSPENDED) {
            this.status = UserStatus.ACTIVE;
        }
    }
    public void activation() {
        if ( this.status == UserStatus.DISABLED) {
            this.status = UserStatus.ACTIVE;
        }
    }

    public void updateTotalSuspensionEndDate(LocalDateTime totalSuspensionEndDate){
        this.totalSuspensionEndDate = totalSuspensionEndDate;
    }
}
