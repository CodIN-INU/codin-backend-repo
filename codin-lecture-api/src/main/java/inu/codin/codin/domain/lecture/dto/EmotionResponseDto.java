package inu.codin.codin.domain.lecture.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmotionResponseDto {

    private double hard = 0.0;
    private double ok = 0.0;
    private double best = 0.0;

    public EmotionResponseDto(double hard, double ok, double best) {
        this.hard = hard;
        this.ok = ok;
        this.best = best;
    }
}
