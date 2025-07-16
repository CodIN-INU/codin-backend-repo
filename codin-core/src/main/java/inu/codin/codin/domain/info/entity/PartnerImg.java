package inu.codin.codin.domain.info.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PartnerImg {

    @Schema(description = "제휴업체 메인 이미지", example = "https://example.com")
    private String main;

    @Schema(description = "제휴업체 서브 이미지", example = "[\"https://example.com\", \"https://example.com\"]")
    private List<String> sub;
}
