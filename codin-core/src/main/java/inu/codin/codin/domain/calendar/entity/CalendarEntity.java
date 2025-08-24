package inu.codin.codin.domain.calendar.entity;

import inu.codin.codin.common.dto.BaseTimeEntity;
import inu.codin.codin.common.dto.Department;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Getter
@Document(collection = "calendar_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CalendarEntity extends BaseTimeEntity {

    @Id
    ObjectId id;

    @Indexed(name = "startDate_idx")
    LocalDate startDate;
    @Indexed(name = "endDate_idx")
    LocalDate endDate;

    private String content;
    private Department department;

    @Builder
    public CalendarEntity(String content, Department department, LocalDate startDate, LocalDate endDate) {
        this.content = content;
        this.department = department;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
