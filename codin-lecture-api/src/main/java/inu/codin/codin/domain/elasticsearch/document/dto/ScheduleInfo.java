package inu.codin.codin.domain.elasticsearch.document.dto;

import inu.codin.codin.domain.lecture.entity.LectureSchedule;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Builder
public class ScheduleInfo {
    @Field(type = FieldType.Keyword)
    private String day;

    @Field(type = FieldType.Keyword)
    private String startTime;

    @Field(type = FieldType.Keyword)
    private String endTime;

    @Field(type = FieldType.Keyword)
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
