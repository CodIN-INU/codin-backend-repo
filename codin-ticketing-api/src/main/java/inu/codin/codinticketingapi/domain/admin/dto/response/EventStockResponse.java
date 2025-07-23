package inu.codin.codinticketingapi.domain.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(description = "이벤트 잔여 수량을 담는 응답 DTO")
public class EventStockResponse {
    @Schema(description = "이벤트 잔여 수량", example = "50")
    private int eventStock;

    public static EventStockResponse of(int eventStock) {
        return EventStockResponse.builder()
                .eventStock(eventStock)
                .build();
    }
}
