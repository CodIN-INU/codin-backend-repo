package inu.codin.codinticketingapi.domain.ticketing.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document(collection = "ticketing-profiles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class TicketingProfile {

    @MongoId
    private ObjectId _id;

    @Indexed
    private ObjectId userId;

    private Department department;
    private String studentId;

    @Builder
    public TicketingProfile(ObjectId userId, Department department, String studentId) {
        this.userId = userId;
        this.department = department;
        this.studentId = studentId;
    }
}