package inu.codin.codin.domain.info.controller;

import inu.codin.codin.common.response.ListResponse;
import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.info.dto.response.PartnerDetailsResponseDto;
import inu.codin.codin.domain.info.dto.response.PartnerListResponseDto;
import inu.codin.codin.domain.info.service.PartnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/info/partner")
@Tag(name = "제휴업체 API", description = "제휴업체 CRUD")
public class PartnerController {

    private final PartnerService partnerService;

    @Operation(
            description = "제휴업체 썸네일 리스트 반환"
    )
    @GetMapping
    public ResponseEntity<ListResponse<PartnerListResponseDto>> getPartnerList(){
        return ResponseEntity.ok()
                .body(new ListResponse<>(200, "Partner 썸네일 리스트 반환 성공", partnerService.getPartnerList()));
    }

    @Operation(
            description = "제휴업체 상세 내역 반환"
    )
    @GetMapping("/{id}")
    public ResponseEntity<SingleResponse<PartnerDetailsResponseDto>> getPartnerDetails(@PathVariable("id") String partnerId){
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "Partner 상세 내열 반환 성공", partnerService.getPartnerDetails(partnerId)));
    }

    /*
    제휴업체 추가, 내용 수정, 삭제
     */
}
