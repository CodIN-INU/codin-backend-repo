package inu.codin.codin.domain.info.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import inu.codin.codin.common.dto.Department;
import inu.codin.codin.domain.info.entity.Partner;
import inu.codin.codin.domain.info.entity.PartnerImg;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
public class PartnerDetailsResponseDto {

    @Schema(description = "제휴업체 가게 이름", example = "홍콩반점 송도점")
    private String name;

    @Schema(description = "제휴 학과", example = "[\"COMPUTER_SCI\", \"INFO_COMM\", \"EMBEDDED\"]")
    private List<Department> tags;

    @Schema(description = "제휴 혜택", example = "[\"탕수육 주문 시, 탕수육 공짜!\", \"평일 언제나 80% 대박 할인!\"]")
    private List<String> benefits;

    @Schema(description = "제휴 시작 날짜", example = "2025-03-01")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "제휴 종료 날짜", example = "2026-03-01")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Schema(description = "제휴업체 가게 위치", example = "인천 연수구 송도동 3-2")
    private String location;

    @Schema(description = "제휴업체 가게 이미지")
    private PartnerImg img;

    public static PartnerDetailsResponseDto from(Partner partner){
        return PartnerDetailsResponseDto.builder()
                .name(partner.getName())
                .tags(partner.getTags())
                .benefits(partner.getBenefits())
                .startDate(partner.getStartDate())
                .endDate(partner.getEndDate())
                .location(partner.getLocation())
                .img(partner.getImg())
                .build();
    }
}
