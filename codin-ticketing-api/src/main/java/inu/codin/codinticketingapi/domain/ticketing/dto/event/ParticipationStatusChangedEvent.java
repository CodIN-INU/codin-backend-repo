package inu.codin.codinticketingapi.domain.ticketing.dto.event;

import inu.codin.codinticketingapi.domain.ticketing.entity.Participation;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ParticipationStatusChangedEvent {

    private final Participation participation;

    public ParticipationStatusChangedEvent(Participation participation) {
        this.participation = participation;
    }
}
