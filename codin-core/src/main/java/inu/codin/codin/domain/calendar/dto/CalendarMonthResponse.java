package inu.codin.codin.domain.calendar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class CalendarMonthResponse {

    @Schema(description = "년도", example = "2025")
    private final int year;

    @Schema(description = "월", example = "8")
    private final int month;

    @Schema(description = "해당 월의 일별 이벤트 목록")
    private final List<CalendarDayResponse> days;

    @Builder
    public CalendarMonthResponse(int year, int month, List<CalendarDayResponse> days) {
        this.year = year;
        this.month = month;
        this.days = days;
    }
}
