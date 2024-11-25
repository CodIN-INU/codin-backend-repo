package inu.codin.codin.infra.fcm.dto;

import inu.codin.codin.domain.user.entity.UserEntity;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

/**
 * Fcm 메시지 DTO
 * 서버 내부 로직에서 사용
 */
@Getter
@Data
public class FcmMessageDto {

    private UserEntity user;
    private String title;
    private String body;

    @Builder
    public FcmMessageDto(UserEntity user, String title, String body) {
        this.user = user;
        this.title = title;
        this.body = body;
    }
}
