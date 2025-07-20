package inu.codin.codinlectureapi.lecture.domain.room.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;

@Document(collection = "lecture_rooms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class LectureRoomEntity {
    private ObjectId _id;
    private String lectureNm;
    private String professor;
    private int roomNum;
    @Field("dayTime")
    private Map<DayOfWeek, List<String>> dayTime;
}
