package inu.codin.codin.domain.notification.entity;

import inu.codin.codin.common.BaseTimeEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "notification")
public class NotificationEntity extends BaseTimeEntity {

    @Id @NotBlank
    private String id;

    private String userId;

    private String type;

    private String message;

    private boolean isRead = false;

    private String priority;

    @Builder
    public NotificationEntity(String userId, String type, String message, String priority) {
        this.userId = userId;
        this.type = type;
        this.message = message;
        this.priority = priority;
    }

    public void read() {
        this.isRead = true;
    }
}
