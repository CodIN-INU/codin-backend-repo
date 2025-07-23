package inu.codin.codinticketingapi.domain.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "이벤트 참여자 목록 및 페이지 정보를 담는 응답 DTO")
public class EventParticipationProfilePageResponse {
    @Schema(description = "이벤트 참여자 프로필 목록")
    private List<EventParticipationProfileResponse> eventParticipationProfileResponseList;
    @Schema(description = "마지막 페이지 번호 (0-based)", example = "1")
    private int lastPage;
    @Schema(description = "다음 페이지 번호 (없으면 -1)", example = "1")
    private int nextPage;

    public static EventParticipationProfilePageResponse of(List<EventParticipationProfileResponse> list, int lastPage, int nextPage) {
        return new EventParticipationProfilePageResponse(list, lastPage, nextPage);
    }
}
