package inu.codin.codin.domain.lecture.domain.room.dto;

import inu.codin.codin.domain.lecture.domain.room.entity.LectureRoomEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmptyRoomResponseDto {
    private String lectureNm;
    private String professor;
    private int roomNum;
    private String startTime;
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
