package inu.codin.codin.domain.lecture.entity;

import inu.codin.codin.common.dto.Department;
import inu.codin.codin.domain.lecture.dto.Emotion;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "lectures")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class LectureEntity {

    private ObjectId _id;
    private String lectureNm;
    private String professor;
    private Department department; //OTHERS : 교양
    private int grade; //0 : 전학년
    private List<String> semester;

    private double starRating;
    private int participants;
    private Emotion emotion = new Emotion();

    public void updateReviewRating(double starRating, int participants, Emotion emotion){
        this.starRating = starRating;
        this.participants = participants;
        this.emotion = emotion;
    }
}
