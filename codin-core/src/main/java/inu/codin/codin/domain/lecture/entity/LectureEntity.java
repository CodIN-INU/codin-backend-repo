package inu.codin.codin.domain.lecture.entity;

import inu.codin.codin.common.BaseTimeEntity;
import inu.codin.codin.common.Department;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;

@Document(collection = "lectures")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class LectureEntity {

    private ObjectId _id;
    private String lectureNm;
    private String lectureCode;
    private String professor;
    private Department department; //OTHERS : 교양
    private int grade; //0 : 전학년
    private int roomNum;

    @Field("dayTime")
    private Map<DayOfWeek, List<String>> dayTime;  // The dayTime map structure

}
