package inu.codin.codin.domain.info.domain.lab.controller;

import inu.codin.codin.common.ResponseUtils;
import inu.codin.codin.domain.info.domain.lab.dto.LabListResponseDto;
import inu.codin.codin.domain.info.domain.lab.dto.LabThumbnailResponseDto;
import inu.codin.codin.domain.info.domain.lab.service.LabService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/info/lab")
@Tag(name = "Info API")
public class LabController {

    private final LabService labService;

    @Operation(summary = "연구실 썸네일 반환")
    @GetMapping("/thumbnail/{id}")
    public ResponseEntity<LabThumbnailResponseDto> getLabThumbnail(@PathVariable("id") String id){
        return ResponseUtils.success(labService.getLabThumbnail(id));
    }

    @Operation(summary = "연구실 리스트 반환")
    @GetMapping
    public ResponseEntity<List<LabListResponseDto>> getAllLab(){
        return ResponseUtils.success(labService.getAllLab());
    }

}
