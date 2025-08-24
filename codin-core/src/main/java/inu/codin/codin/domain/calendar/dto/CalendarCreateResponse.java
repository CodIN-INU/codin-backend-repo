package inu.codin.codin.domain.calendar.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import inu.codin.codin.common.dto.Department;
import inu.codin.codin.common.util.ObjectIdUtil;
import inu.codin.codin.domain.calendar.entity.CalendarEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CalendarCreateResponse {

    @Schema(description = "이벤트 ID", example = "68ab0ac4ffbcdc7080e8d663")
    private final String eventId;

    @Schema(description = "이벤트 시작 날짜", example = "2025-08-23")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private final LocalDate startDate;

    @Schema(description = "이벤트 종료 날짜", example = "2025-08-29")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private final LocalDate endDate;

    @Schema(description = "이벤트 내용", example = "테스트 이벤트")
    private final String content;

    @Schema(description = "학과 (한글 가능)", example = "COMPUTER_SCI")
    private final Department department;

    @Builder
    public CalendarCreateResponse(String eventId, LocalDate startDate, LocalDate endDate, String content, Department department) {
        this.eventId = eventId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.content = content;
        this.department = department;
    }

    public static CalendarCreateResponse of(CalendarEntity calendar) {
        return CalendarCreateResponse.builder()
                .eventId(ObjectIdUtil.toString(calendar.getId()))
                .startDate(calendar.getStartDate())
                .endDate(calendar.getEndDate())
                .content(calendar.getContent())
                .department(calendar.getDepartment() != null ? calendar.getDepartment() : null)
                .build();
    }
}
