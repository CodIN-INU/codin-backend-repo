package inu.codin.codin.infra.fcm.dto;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

import java.util.Map;

/**
 * Fcm 메시지 DTO to User
 * 서버 내부 로직에서 사용
 */
@Data
public class FcmMessageUserDto {

    private ObjectId userId;
    private String title;
    private String body;
    private String imageUrl;
    private Map<String, String> data;

    @Builder
    public FcmMessageUserDto(ObjectId userId, String title, String body, String imageUrl, Map<String, String> data) {
        this.userId = userId;
        this.title = title;
        this.body = body;
        this.imageUrl = imageUrl;
        this.data = data;
    }
}
