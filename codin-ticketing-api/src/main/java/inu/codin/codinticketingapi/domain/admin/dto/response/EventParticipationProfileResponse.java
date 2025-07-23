package inu.codin.codinticketingapi.domain.admin.dto.response;

import inu.codin.codinticketingapi.domain.ticketing.entity.Department;
import inu.codin.codinticketingapi.domain.ticketing.entity.Participation;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EventParticipationProfileResponse {
    private String userId;
    private String name;
    private String studentId;
    private Department department;
    private String imageURL;

    public static EventParticipationProfileResponse of(Participation participation) {
        return EventParticipationProfileResponse.builder()
                .userId(participation.getUserId())
                .name(participation.getName())
                .studentId(participation.getStudentId())
                .department(participation.getDepartment())
                .imageURL(participation.getSignatureImgUrl())
                .build();
    }
}
