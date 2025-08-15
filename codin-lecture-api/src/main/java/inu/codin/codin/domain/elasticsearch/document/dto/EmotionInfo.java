package inu.codin.codin.domain.elasticsearch.document.dto;

import inu.codin.codin.domain.lecture.entity.Emotion;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Builder
public class EmotionInfo {
    @Field(type = FieldType.Integer)
    private int hard;

    @Field(type = FieldType.Integer)
    private int ok;

    @Field(type = FieldType.Integer)
    private int best;

    public static EmotionInfo from(Emotion emotion) {
        if (emotion == null) {
            return null;
        }

        return EmotionInfo.builder()
                .hard(emotion.getHard())
                .ok(emotion.getOk())
                .best(emotion.getBest())
                .build();
    }
}
