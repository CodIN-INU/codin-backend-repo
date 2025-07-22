package inu.codin.codinticketingapi.domain.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class EventParticipationProfilePageResponse {
    private List<EventParticipationProfileResponse> eventParticipationProfileResponseList;
    private int lastPage;
    private int nextPage;

    public static EventParticipationProfilePageResponse of(List<EventParticipationProfileResponse> list, int lastPage, int nextPage) {
        return new EventParticipationProfilePageResponse(list, lastPage, nextPage);
    }
}
