package inu.codin.codin.domain.notification.dto;

import inu.codin.codin.domain.notification.entity.NotificationEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;

@Getter
public class NotificationListResponseDto {
    @Id
    @NotBlank
    private String id;

    private String targetId;

    private String title;

    private String message;

    private boolean isRead = false;

    @Builder
    public NotificationListResponseDto(String id, String targetId, String title, String message, boolean isRead) {
        this.id = id;
        this.targetId = targetId;
        this.title = title;
        this.message = message;
        this.isRead = isRead;
    }

    public static NotificationListResponseDto of(NotificationEntity notificationEntity){
        return NotificationListResponseDto.builder()
                .id(notificationEntity.getId().toString())
                .title(notificationEntity.getTitle())
                .message(notificationEntity.getMessage())
                .targetId(notificationEntity.getTargetId().toString())
                .isRead(notificationEntity.isRead())
                .build();
    }
}
