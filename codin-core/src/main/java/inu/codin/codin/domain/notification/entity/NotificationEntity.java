package inu.codin.codin.domain.notification.entity;

import inu.codin.codin.common.BaseTimeEntity;
import inu.codin.codin.domain.user.entity.UserEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@Document(collection = "notification")
public class NotificationEntity extends BaseTimeEntity {

    @Id @NotBlank
    private ObjectId id;

    @DBRef(lazy = true)
    private UserEntity user;

    private String title;

    private String message;

    private boolean isRead = false;

    private LocalDateTime readAt;

    // push, email ...
    private String type;

    // 알림 중요도 - 미사용중
    private String priority;

    @Builder
    public NotificationEntity(UserEntity user, String title, String message, String type, String priority) {
        this.user = user;
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
