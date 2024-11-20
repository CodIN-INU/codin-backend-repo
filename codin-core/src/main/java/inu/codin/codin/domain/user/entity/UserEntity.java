package inu.codin.codin.domain.user.entity;

import inu.codin.codin.common.BaseTimeEntity;
import inu.codin.codin.common.Department;
import inu.codin.codin.domain.notification.entity.NotificationPreference;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@Getter
public class UserEntity extends BaseTimeEntity {

    @Id @NotBlank
    private String id;

    private String email;

    private String password;

    private String studentId;

    private String name;

    private String nickname;

    private String profileImageUrl;

    private Department department;

    private UserRole role;

    private UserStatus status;

    // 알림 설정
    private NotificationPreference notificationPreference = new NotificationPreference();

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
}
