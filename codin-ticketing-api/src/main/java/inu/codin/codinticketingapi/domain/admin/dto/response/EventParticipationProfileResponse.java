package inu.codin.codinticketingapi.domain.admin.dto.response;

import inu.codin.codinticketingapi.domain.ticketing.entity.Department;
import inu.codin.codinticketingapi.domain.ticketing.entity.Profile;
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

    public static EventParticipationProfileResponse of(Profile profile, String imageURL) {
        return EventParticipationProfileResponse.builder()
                .userId(profile.getUserId())
                .name(profile.getName())
                .studentId(profile.getStudentId())
                .department(profile.getDepartment())
                .imageURL(imageURL)
                .build();
    }
}
