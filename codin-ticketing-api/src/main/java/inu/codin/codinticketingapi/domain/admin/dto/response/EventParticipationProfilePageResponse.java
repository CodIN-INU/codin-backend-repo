package inu.codin.codinticketingapi.domain.admin.dto.response;

import inu.codin.codinticketingapi.domain.admin.entity.Event;
import inu.codin.codinticketingapi.domain.ticketing.entity.Stock;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
@Schema(description = "이벤트 참여자 목록 및 페이지 정보를 담는 응답 DTO")
public class EventParticipationProfilePageResponse {
    @Schema(description = "이벤트 참여자 프로필 목록")
    private List<EventParticipationProfileResponse> eventParticipationProfileResponseList;
    @Schema(description = "마지막 페이지 번호 (0-based)", example = "1")
    private int lastPage;
    @Schema(description = "다음 페이지 번호 (없으면 -1)", example = "1")
    private int nextPage;
    @Schema(description = "이벤트 이름", example = "새내기를 위한 이벤트")
    private String title;
    @Schema(description = "잔여 수량", example = "10")
    private int stock;
    @Schema(description = "수령 대기", example = "100")
    private long waitNum;

    @Schema(description = "이벤트 티켓팅 시작 시간", example = "2025-07-25T12:00:00")
    private LocalDateTime eventStartTime;
    @Schema(description = "이벤트 티켓팅 종료 시간", example = "2025-07-25T12:00:00")
    private LocalDateTime eventEndTime;
    @Schema(description = "티켓팅 상품 수령 시작 시간", example = "2025-07-25T12:00:00")
    private LocalDateTime eventReceivedStartTime;
    @Schema(description = "티켓팅 상품 수령 종료 시간", example = "2025-07-25T12:00:00")
    private LocalDateTime eventReceivedEndTime;

    public static EventParticipationProfilePageResponse from(Event event, Stock stock, List<EventParticipationProfileResponse> list, int lastPage, int nextPage, long waitNum) {
        return EventParticipationProfilePageResponse.builder()
                .eventParticipationProfileResponseList(list)
                .lastPage(lastPage)
                .nextPage(nextPage)
                .waitNum(waitNum)
                .eventStartTime(event.getEventTime())
                .eventEndTime(event.getEventEndTime())
                .eventReceivedStartTime(event.getEventReceivedStartTime())
                .eventReceivedEndTime(event.getEventReceivedEndTime())
                .build();
    }
}
