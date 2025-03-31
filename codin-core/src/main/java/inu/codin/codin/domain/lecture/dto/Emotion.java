package inu.codin.codin.domain.lecture.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Emotion {

    private double hard;
    private double ok;
    private double best;


    public Emotion() {
        this.hard = 0;
        this.ok = 0;
        this.best = 0;
    }

    @Builder
    public Emotion(double hard, double ok, double best) {
        this.hard = hard;
        this.ok = ok;
        this.best = best;
    }

    public Emotion changeToPercentage(){
        double total = hard + ok + best;
        if (total > 0) {
            this.hard = (hard / total) * 100;
            this.ok = (ok / total) * 100;
            this.best = (best / total) * 100;
        }
        return this;
    }

}
