package inu.codin.codin.domain.info.controller;

import inu.codin.codin.common.response.ListResponse;
import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.info.dto.request.PartnerCreateRequestDto;
import inu.codin.codin.domain.info.dto.response.PartnerDetailsResponseDto;
import inu.codin.codin.domain.info.dto.response.PartnerListResponseDto;
import inu.codin.codin.domain.info.service.PartnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/info/partner")
@Tag(name = "Partner API", description = "Partner CRUD")
public class PartnerController {

    private final PartnerService partnerService;

    @Operation(
            summary = "Partner 썸네일 리스트 반환"
    )
    @GetMapping
    public ResponseEntity<ListResponse<PartnerListResponseDto>> getPartnerList(){
        return ResponseEntity.ok()
                .body(new ListResponse<>(200, "Partner 썸네일 리스트 반환 성공", partnerService.getPartnerList()));
    }

    @Operation(
            summary = "Partner 상세 내역 반환"
    )
    @GetMapping("/{id}")
    public ResponseEntity<SingleResponse<PartnerDetailsResponseDto>> getPartnerDetails(@PathVariable("id") String partnerId){
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "Partner 상세 내열 반환 성공", partnerService.getPartnerDetails(partnerId)));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(
            summary = "[ADMIN, MANAGER] Partner 추가"
    )
    @PostMapping
    public ResponseEntity<SingleResponse<?>> createPartner(@RequestBody @Valid PartnerCreateRequestDto partnerCreateRequestDto){
        partnerService.createPartner(partnerCreateRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SingleResponse<>(201, "Partner 생성 완료", null));
    }

    /*
    제휴업체 추가, 내용 수정, 삭제
     */
}
