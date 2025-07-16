package inu.codin.codin.domain.info.dto.response;

import inu.codin.codin.common.dto.Department;
import inu.codin.codin.domain.info.entity.Partner;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class PartnerListResponseDto {

    @Schema(description = "제휴업체 pk", example = "6862c1128d0ca733396ecd5b")
    @NotBlank
    private String id;

    @Schema(description = "제휴업체 가게 이름", example = "홍콩반점 송도점")
    private String name;

    @Schema(description = "제휴업체 가게 이미지", example = "https://example.com")
    private String mainImg;

    @Schema(description = "제휴 학과", example = "[\"COMPUTER_SCI\", \"INFO_COMM\", \"EMBEDDED\"]")
    private List<Department> tags;

    public static PartnerListResponseDto from(Partner partner) {
        return PartnerListResponseDto.builder()
                .id(partner.get_id().toString())
                .name(partner.getName())
                .mainImg(partner.getImg().getMain())
                .tags(partner.getTags())
                .build();
    }
}
