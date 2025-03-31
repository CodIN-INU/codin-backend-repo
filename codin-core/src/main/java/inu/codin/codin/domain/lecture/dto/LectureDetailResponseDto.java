package inu.codin.codin.domain.lecture.dto;

import inu.codin.codin.domain.lecture.entity.LectureEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
public class LectureDetailResponseDto extends LectureListResponseDto{

    @Schema(description = "후기 평점들의 범위마다 100분율 계산", example = "hard : 30, ok : 20, best : 50")
    private Emotion emotion;

    public LectureDetailResponseDto(String _id, String lectureNm, String professor, double starRating, int participants, List<String> semesters, int grade, Emotion emotion) {
        super(_id, lectureNm, professor, starRating, participants, semesters, grade);
        this.emotion = emotion;
    }

    public static LectureDetailResponseDto of(LectureEntity lectureEntity){
        return new LectureDetailResponseDto(
                lectureEntity.get_id().toString(),
                lectureEntity.getLectureNm(),
                lectureEntity.getProfessor(),
                lectureEntity.getStarRating(), //평균 평점
                lectureEntity.getParticipants(), //참여 인원 수
                lectureEntity.getSemester(),
                lectureEntity.getGrade(),
                lectureEntity.getEmotion() //평점 당 인원 평균
        );
    }
}
