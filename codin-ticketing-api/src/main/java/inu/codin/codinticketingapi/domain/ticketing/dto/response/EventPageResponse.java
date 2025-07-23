package inu.codin.codinticketingapi.domain.ticketing.dto.response;

import inu.codin.codinticketingapi.domain.admin.entity.Event;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventPageResponse {

    @Schema(description = "이벤트 목록")
    private List<EventResponse> eventList = new ArrayList<>();

    @Schema(description = "마지막 페이지 인덱스", example = "0")
    private long lastPage;

    @Schema(description = "다음 페이지 인덱스", example = "-1")
    private long nextPage;

    public EventPageResponse(List<EventResponse> eventList, long lastPage, long nextPage) {
        this.eventList = eventList;
        this.lastPage = lastPage;
        this.nextPage = nextPage;
    }

    public static EventPageResponse of(List<EventResponse> eventList, long lastPage, long nextPage) {
        return new EventPageResponse(eventList, lastPage, nextPage);
    }

    public static EventPageResponse of(Page<Event> page) {
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