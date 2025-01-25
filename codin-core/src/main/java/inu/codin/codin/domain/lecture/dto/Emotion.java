package inu.codin.codin.domain.lecture.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Emotion {

    private double hard;
    private double ok;
    private double best;

    @Builder
    public Emotion(double hard, double ok, double best) {
        this.hard = hard;
        this.ok = ok;
        this.best = best;
    }

}
