package inu.codin.codin.domain.lecture.dto;

import inu.codin.codin.domain.lecture.entity.LectureSchedule;
import inu.codin.codin.domain.lecture.entity.Lecture;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LectureRoomResponseDto {

    @Schema(description = "강의명", example = "Java")
    private String lectureNm;

    @Schema(description = "교수명", example = "홍길동")
    private String professor;

    @Schema(description = "강의실 호수", example = "419")
    private int roomNum;

    @Schema(description = "시작 시간", example = "09:00")
    private String startTime;

    @Schema(description = "종료 시간", example = "18:00")
    private String endTime;

    @Builder
    public LectureRoomResponseDto(String lectureNm, String professor, int roomNum, String startTime, String endTime) {
        this.lectureNm = lectureNm;
        this.professor = professor;
        this.roomNum = roomNum;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static LectureRoomResponseDto of(Lecture lecture, int room, LectureSchedule schedule) {
        return LectureRoomResponseDto.builder()
                .lectureNm(lecture.getLectureNm())
                .professor(lecture.getProfessor())
                .roomNum(room)
                .startTime(schedule.getStart())
                .endTime(schedule.getEnd())
                .build();
    }

}
