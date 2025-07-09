package inu.codin.codinticketingapi.domain.ticketing.entity;

import inu.codin.codinticketingapi.common.BaseTimeEntity;
import lombok.Builder;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "ticketing-events")
@Getter
public class TicketingEvent extends BaseTimeEntity {
    //todo: campus, deletedAt, createdAt 으로 복합인덱스 설정

    @MongoId
    private ObjectId _id;

    @Indexed
    private final ObjectId userId;
    @Indexed
    private Campus campus;
    @Indexed
    private LocalDateTime eventTime;

    private List<String> eventImageUrls;
    private String title;
    private String locationInfo;
    private int quantity;
    private String target;
    private String description;
    private String inquiryNumber;
    private String promotionLink;

    private final String eventPassword;

    @Builder
    public TicketingEvent(ObjectId userId, Campus campus, LocalDateTime eventTime, List<String> eventImageUrls, String title, String locationInfo, int quantity, String target, String description, String inquiryNumber, String promotionLink, String eventPassword) {
        this.userId = userId;
        this.campus = campus;
        this.eventTime = eventTime;
        this.eventImageUrls = eventImageUrls;
        this.title = title;
        this.locationInfo = locationInfo;
        this.quantity = quantity;
        this.target = target;
        this.description = description;
        this.inquiryNumber = inquiryNumber;
        this.promotionLink = promotionLink;
        this.eventPassword = eventPassword;
    }
}
