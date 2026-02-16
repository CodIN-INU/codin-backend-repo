package inu.codin.codin.domain.elasticsearch.document.dto;

import inu.codin.codin.domain.lecture.entity.LectureSchedule;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleInfo {
    private String day;
    private String startTime;
    private String endTime;
    private String roomInfo;

    public static ScheduleInfo from(LectureSchedule schedule) {

        return ScheduleInfo.builder()
                .day(schedule.getDayOfWeek().name())
                .startTime(schedule.getStart())
                .endTime(schedule.getEnd())
                .roomInfo(schedule.getRoom() != null ? String.valueOf(schedule.getRoom().getRoomNum()) : "미정")
                .build();
    }
}
