package inu.codin.codin.domain.notification.entity;

import inu.codin.codin.common.BaseTimeEntity;
import inu.codin.codin.domain.user.entity.UserEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "notification")
public class NotificationEntity extends BaseTimeEntity {

    @Id @NotBlank
    private String id;

    @DBRef(lazy = true)
    private UserEntity user;

    private String type;

    private String message;

    private boolean isRead = false;

    private String priority;

    @Builder
    public NotificationEntity(UserEntity user, String type, String message, String priority) {
        this.user = user;
        this.type = type;
        this.message = message;
        this.priority = priority;
    }

    public void read() {
        this.isRead = true;
    }
}
