package inu.codin.codin.infra.fcm.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Fcm 메시지 DTO to Topic
 * 서버 내부 로직에서 사용
 */
@Data
public class FcmMessageTopicDto {

    private String topic;
    private String title;
    private String body;
    private String imageUrl;
    private Map<String, String> data;

    @Builder
    public FcmMessageTopicDto(String topic, String title, String body, String imageUrl, Map<String, String> data) {
        this.topic = topic;
        this.title = title;
        this.body = body;
        this.imageUrl = imageUrl;
        this.data = data;
    }
}
