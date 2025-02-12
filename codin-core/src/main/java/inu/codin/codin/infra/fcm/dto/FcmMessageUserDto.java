package inu.codin.codin.infra.fcm.dto;

import inu.codin.codin.domain.user.entity.UserEntity;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Fcm 메시지 DTO to User
 * 서버 내부 로직에서 사용
 */
@Data
public class FcmMessageUserDto {

    private UserEntity user;
    private String title;
    private String body;
    private String imageUrl;
    private Map<String, String> data;

    @Builder
    public FcmMessageUserDto(UserEntity user, String title, String body, String imageUrl, Map<String, String> data) {
        this.user = user;
        this.title = title;
        this.body = body;
        this.imageUrl = imageUrl;
        this.data = data;
    }
}
