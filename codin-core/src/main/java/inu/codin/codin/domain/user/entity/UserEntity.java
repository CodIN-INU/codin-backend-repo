package inu.codin.codin.domain.user.entity;

import inu.codin.codin.domain.notification.entity.NotificationPreference;
import inu.codin.codin.domain.user.dto.request.SetUserInfoRequestDto;
import inu.codin.codin.domain.user.dto.request.UserTicketingParticipationInfoUpdateRequest;
import inu.codin.common.contract.PortalLoginResponseDto;
import inu.codin.common.entity.BaseTimeEntity;
import inu.codin.common.entity.College;
import inu.codin.common.entity.Department;
import inu.codin.security.entity.UserRole;
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

    private College college;

    private UserRole role;

    private UserStatus status;

    private LocalDateTime totalSuspensionEndDate; //정지 게시물이 늘어날수록 정지 종료일이 중첩

    private NotificationPreference notificationPreference = new NotificationPreference();

    @Builder
    public UserEntity(String email, String password, String studentId, String name, String nickname, String profileImageUrl, College college, Department department, UserRole role, UserStatus status) {
        this.email = email;
        this.password = password;
        this.studentId = studentId;
        this.name = name;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.college = college;
        this.department = department;
        this.role = role;
        this.status = status;
    }

    public void setUserInfo(SetUserInfoRequestDto setUserInfoRequestDto) {
        this.nickname = setUserInfoRequestDto.nickname();
        this.name = setUserInfoRequestDto.name();
        this.college = setUserInfoRequestDto.college();
        this.department = setUserInfoRequestDto.department();
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateCollege(College college) {
        this.college = college;
    }

    public void updateDepartment(Department department) {
        this.department = department;
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

    /**
     * studentId(학번), name(실제 이름), department(소속) 정보 업데이트
     * @param updateRequest 유저 티켓팅 참여 정보 업데이트 request entity
     */
    public void updateParticipationInfo(UserTicketingParticipationInfoUpdateRequest updateRequest) {
        this.studentId = updateRequest.getStudentId();
        this.department = updateRequest.getDepartment();
        this.name = updateRequest.getName();
    }
}
