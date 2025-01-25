package inu.codin.codin.domain.lecture.dto;

import inu.codin.codin.domain.lecture.entity.LectureEntity;

public class LectureDetailResponseDto extends LectureListResponseDto{

    public LectureDetailResponseDto(String _id, String lectureNm, String professor, int starRating, int participants) {
        super(_id, lectureNm, professor, starRating, participants);
    }

    public static LectureDetailResponseDto of(LectureEntity lectureEntity){
        return new LectureDetailResponseDto(
                lectureEntity.get_id().toString(),
                lectureEntity.getLectureNm(),
                lectureEntity.getProfessor(),
                0,0)
                ;
    }
}
