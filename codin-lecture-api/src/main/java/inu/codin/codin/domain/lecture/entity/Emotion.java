package inu.codin.codin.domain.lecture.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
public class Emotion {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private double hard;
    private double ok;
    private double best;

    @OneToOne
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

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
