package inu.codin.codin.domain.calendar.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import inu.codin.common.entity.Department;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CalendarCreateRequest {

    @Schema(description = "이벤트 시작 날짜", example = "2025-08-23")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull
    private final LocalDate startDate;

    @Schema(description = "이벤트 종료 날짜", example = "2025-08-29")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull
    private final LocalDate endDate;

    @Schema(description = "이벤트 내용", example = "테스트 이벤트")
    @NotBlank
    private final String content;

    @Schema(description = "학과 (없으면 전체 행사로 처리)", example = "컴퓨터공학부", nullable = true)
    private final Department department;

    @Builder
    public CalendarCreateRequest(LocalDate startDate, LocalDate endDate, String content, Department department) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.content = content;
        this.department = department;
    }
}
