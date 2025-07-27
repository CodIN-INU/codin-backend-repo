package inu.codin.codin.domain.lecture.entity;

import inu.codin.codin.domain.lecture.dto.EmotionResponseDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Emotion {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int hard;
    private int ok;
    private int best;

    @OneToOne
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    public Emotion(Lecture lecture) {
        this.hard = 0;
        this.ok = 0;
        this.best = 0;
        this.lecture = lecture;
    }

    public EmotionResponseDto changeToPercentage(){
        double total = hard + ok + best;
        if (total > 0) {
            double hard = (this.hard / total) * 100;
            double ok = (this.ok / total) * 100;
            double best = (this.best / total) * 100;
            return new EmotionResponseDto(hard, ok, best);
        }
        return new EmotionResponseDto();
    }

    public void updateScore(double starRating) {
        if (starRating >= 0.25 && starRating <= 1.5) this.hard++;
        else if (starRating >= 1.75 && starRating <= 3.5) this.ok++;
        else if (starRating >= 3.75 && starRating <= 5.0) this.best++;
    }
}
