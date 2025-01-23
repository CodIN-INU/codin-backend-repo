package inu.codin.codin.domain.lecture.dto;

import inu.codin.codin.domain.lecture.entity.LectureEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmptyRoomResponseDto {
    private String lectureNm;
    private int roomNum;
    private String startTime;
    private String endTime;

    @Builder
    public EmptyRoomResponseDto(String lectureNm, int roomNum, String startTime, String endTime) {
        this.lectureNm = lectureNm;
        this.roomNum = roomNum;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static EmptyRoomResponseDto of(LectureEntity lectureEntity, String time) {
        return EmptyRoomResponseDto.builder()
                .lectureNm(lectureEntity.getLectureNm())
                .roomNum(lectureEntity.getRoomNum())
                .startTime(time.split("/")[0])
                .endTime(time.split("/")[1])
                .build();
    }

}
