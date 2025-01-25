package inu.codin.codin.domain.lecture.dto;

import inu.codin.codin.domain.lecture.entity.LectureEntity;
import lombok.Getter;

@Getter
public class LectureDetailResponseDto extends LectureListResponseDto{

    private Emotion emotion;

    public LectureDetailResponseDto(String _id, String lectureNm, String professor, double starRating, long participants, Emotion emotion) {
        super(_id, lectureNm, professor, starRating, participants);
        this.emotion = emotion;
    }

    public static LectureDetailResponseDto of(LectureEntity lectureEntity, double ave, Emotion emotion, long participants){
        return new LectureDetailResponseDto(
                lectureEntity.get_id().toString(),
                lectureEntity.getLectureNm(),
                lectureEntity.getProfessor(),
                ave, //평균 평점
                participants, //참여 인원 수
                emotion //평점 당 인원 평균
        );
    }
}
