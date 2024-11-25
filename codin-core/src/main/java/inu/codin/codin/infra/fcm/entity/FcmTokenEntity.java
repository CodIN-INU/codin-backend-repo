package inu.codin.codin.infra.fcm.entity;

import inu.codin.codin.common.BaseTimeEntity;
import inu.codin.codin.domain.user.entity.UserEntity;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "fcmToken")
@Getter
public class FcmTokenEntity extends BaseTimeEntity {

    @Id
    private String id;

    @DBRef(lazy = true)
    private UserEntity user;

    private String fcmToken;

    private String deviceType;

    @Builder
    public FcmTokenEntity(UserEntity user, String fcmToken, String deviceType) {
        this.user = user;
        this.fcmToken = fcmToken;
        this.deviceType = deviceType;
    }
}
