package inu.codin.codin.domain.info.dto.request;

import inu.codin.codin.common.dto.Department;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class PartnerCreateRequestDto {

    @NotBlank
    @Schema(description = "제휴업체 가게 이름", example = "홍콩반점 송도점")
    private String name;

    @NotEmpty
    @Schema(description = "제휴 학과", example = "[\"COMPUTER_SCI\", \"INFO_COMM\", \"EMBEDDED\"]")
    private List<Department> tags;

    @NotEmpty
    @Schema(description = "제휴 혜택", example = "[\"탕수육 주문 시, 탕수육 공짜!\", \"평일 언제나 80% 대박 할인!\"]")
    private List<String> benefits;

    @Schema(description = "제휴 시작 날짜", example = "2025-03-01")
    private LocalDate startDate;

    @Schema(description = "제휴 종료 날짜", example = "2026-03-01")
    private LocalDate endDate;

    @Schema(description = "제휴업체 가게 위치", example = "인천 연수구 송도동 3-2")
    private String location;
}
