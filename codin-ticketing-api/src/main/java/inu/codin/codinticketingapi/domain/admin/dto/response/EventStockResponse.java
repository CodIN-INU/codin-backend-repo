package inu.codin.codinticketingapi.domain.admin.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EventStockResponse {
    private int eventStock;

    public static EventStockResponse of(int eventStock) {
        return EventStockResponse.builder()
                .eventStock(eventStock)
                .build();
    }
}
