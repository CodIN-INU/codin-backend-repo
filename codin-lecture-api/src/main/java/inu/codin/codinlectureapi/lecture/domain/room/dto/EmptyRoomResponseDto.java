package inu.codin.codinlectureapi.lecture.domain.room.dto;

import inu.codin.codin.domain.lecture.domain.room.entity.LectureRoomEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmptyRoomResponseDto {

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
    public EmptyRoomResponseDto(String lectureNm, String professor, int roomNum, String startTime, String endTime) {
        this.lectureNm = lectureNm;
        this.professor = professor;
        this.roomNum = roomNum;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static EmptyRoomResponseDto of(LectureRoomEntity roomEntity, String time) {
        return EmptyRoomResponseDto.builder()
                .lectureNm(roomEntity.getLectureNm())
                .professor(roomEntity.getProfessor())
                .roomNum(roomEntity.getRoomNum())
                .startTime(time.split("/")[0])
                .endTime(time.split("/")[1])
                .build();
    }

}
