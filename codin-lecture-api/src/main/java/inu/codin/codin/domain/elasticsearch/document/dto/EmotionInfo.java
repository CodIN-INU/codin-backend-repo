package inu.codin.codin.domain.elasticsearch.document.dto;

import inu.codin.codin.domain.lecture.entity.Emotion;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class EmotionInfo {
    private int hard;
    private int ok;
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
