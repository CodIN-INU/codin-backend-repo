package inu.codin.codinticketingapi.domain.ticketing.dto.response;

import inu.codin.codinticketingapi.domain.ticketing.entity.TicketingEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventPageResponse {

    private List<EventResponse> eventList = new ArrayList<>();
    private long lastPage;
    private long nextPage;

    public EventPageResponse(List<EventResponse> eventList, long lastPage, long nextPage) {
        this.eventList = eventList;
        this.lastPage = lastPage;
        this.nextPage = nextPage;
    }

    public static EventPageResponse of(List<EventResponse> eventList, long lastPage, long nextPage) {
        return new EventPageResponse(eventList, lastPage, nextPage);
    }

    public static EventPageResponse of(Page<TicketingEvent> page) {
        List<EventResponse> eventList = page.getContent().stream()
                .map(EventResponse::of)
                .toList();

        return new EventPageResponse(
                eventList,
                page.getTotalPages() - 1, // 0-based index이므로 -1
                page.hasNext() ? page.getNumber() + 1 : -1 // 다음 페이지가 없으면 -1
        );
    }
}