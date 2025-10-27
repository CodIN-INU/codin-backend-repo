package inu.codin.codinticketingapi.domain.ticketing.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "티켓팅 이벤트 참여 기록 페이지 응답 DTO")
public class EventParticipationHistoryPageResponse {

    @Schema(description = "참여 이벤트 목록")
    private final List<EventParticipationHistoryDto> eventList;

    @Schema(description = "마지막 페이지 인덱스", example = "0")
    private final long lastPage;

    @Schema(description = "다음 페이지 인덱스", example = "-1")
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