package inu.codin.codin.domain.user.entity;

import inu.codin.codin.common.dto.BaseTimeEntity;
import inu.codin.codin.common.dto.Department;
import inu.codin.codin.common.security.dto.PortalLoginResponseDto;
import inu.codin.codin.domain.notification.entity.NotificationPreference;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseTimeEntity {

    @Id @NotNull
    private ObjectId _id;

    @NotBlank
    private String email;

    private String password;

    private String studentId;

    @NotBlank
    private String name;

    @NotBlank
    private String nickname;

    @NotBlank
    private String profileImageUrl;

    @NotNull
    private Department department;

    private String college;

    @NotNull
    private UserRole role;

    @NotNull
    private UserStatus status;

    private LocalDateTime totalSuspensionEndDate; //정지 게시물이 늘어날수록 정지 종료일이 중첩

    private List<ObjectId> blockedUsers;

    private final NotificationPreference notificationPreference = new NotificationPreference();

    @Builder
    public UserEntity(String email, String password, String studentId, String name, String nickname, String profileImageUrl, Department department, String college, UserRole role, UserStatus status, List<ObjectId> blockedUsers) {
        this.email = email;
        this.password = password;
        this.studentId = studentId;
        this.name = name;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.department = department;
        this.college = college;
        this.role = role;
        this.status = status;
        this.blockedUsers = (blockedUsers != null) ? blockedUsers : new ArrayList<>(); // ✅ 기본값 설정
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
                .nickname("")
                .profileImageUrl("")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .blockedUsers(new ArrayList<>())
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
