package inu.codin.codinticketingapi.domain.ticketing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
public class EventParticipationHistoryPageResponse {

    private final List<EventParticipationHistoryDto> eventList;
    private final long lastPage;
    private final long nextPage;

    public static EventParticipationHistoryPageResponse of(List<EventParticipationHistoryDto> eventList, long lastPage, long nextPage) {
        return new EventParticipationHistoryPageResponse(eventList, lastPage, nextPage);
    }

    public static EventParticipationHistoryPageResponse of(Page<EventParticipationHistoryDto> page) {
        List<EventParticipationHistoryDto> content = page.getContent();
        long lastPageIndex = page.getTotalPages() > 0 ? page.getTotalPages() - 1 : 0;
        long nextPageIndex = page.hasNext() ? page.getNumber() + 1 : -1;

        return new EventParticipationHistoryPageResponse(
                content,
                lastPageIndex,
                nextPageIndex
        );
    }
}