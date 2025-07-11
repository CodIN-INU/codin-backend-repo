package inu.codin.codinticketingapi.domain.ticketing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class EventParticipationHistoryDto {
    private Long eventId;
    private String title;
    private String eventImageUrl;
    private String locationInfo;
    private LocalDateTime eventTime;
    private LocalDateTime eventEndTime;
    private boolean confirmed;
}
