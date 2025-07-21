package inu.codin.codinticketingapi.domain.admin.dto.response;

import inu.codin.codinticketingapi.domain.admin.entity.Event;
import inu.codin.codinticketingapi.domain.admin.entity.EventStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class EventResponse {
    private Long id;
    private String campus;
    private LocalDateTime eventTime;
    private LocalDateTime eventEndTime;
    private String eventImageUrl;
    private String title;
    private String locationInfo;
    private int stock;
    private String target;
    private String description;
    private String inquiryNumber;
    private String promotionLink;
    private EventStatus status;

    public static EventResponse of(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .campus(event.getCampus().getDescription())
                .eventTime(event.getEventTime())
                .eventEndTime(event.getEventEndTime())
                .eventImageUrl(event.getEventImageUrl())
                .title(event.getTitle())
                .locationInfo(event.getLocationInfo())
                .stock(event.getStock().getStock())
                .target(event.getTarget())
                .description(event.getDescription())
                .inquiryNumber(event.getInquiryNumber())
                .promotionLink(event.getPromotionLink())
                .status(event.getEventStatus())
                .build();
    }
}
