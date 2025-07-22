package inu.codin.codinticketingapi.domain.ticketing.dto.event;

import inu.codin.codinticketingapi.domain.admin.entity.Event;
import lombok.Getter;

@Getter
public class StockDecrementRequest {
    Event event;

    public StockDecrementRequest(Event event) {
        this.event = event;
    }
}
