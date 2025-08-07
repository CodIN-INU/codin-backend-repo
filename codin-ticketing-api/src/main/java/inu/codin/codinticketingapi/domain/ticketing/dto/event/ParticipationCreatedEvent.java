package inu.codin.codinticketingapi.domain.ticketing.dto.event;

import inu.codin.codinticketingapi.domain.ticketing.entity.Participation;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ParticipationCreatedEvent {

    private final Participation participation;

    public ParticipationCreatedEvent(Participation participation) {
        this.participation = participation;
    }
}
