package inu.codin.codin.domain.notification.entity;

import inu.codin.codin.common.dto.BaseTimeEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@Document(collection = "notification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class NotificationEntity extends BaseTimeEntity {

    @Id @NotBlank
    private ObjectId id;

    private ObjectId userId;

    private ObjectId targetId;

    private String title;

    private String message;

    private boolean isRead = false;

    private LocalDateTime readAt;

    // push, email ...
    private String type;

    // 알림 중요도 - 미사용중
    private String priority;

    @Builder
    public NotificationEntity(ObjectId userId, ObjectId targetId, String title, String message, String type, String priority) {
        this.userId = userId;
        this.targetId = targetId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.priority = priority;
    }

    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
}
