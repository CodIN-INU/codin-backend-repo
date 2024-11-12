package inu.codin.codin.domain.info.lab.controller;

import inu.codin.codin.domain.info.lab.service.LabService;
import inu.codin.codin.domain.info.lab.dto.LabThumbnailResDTO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lab")
public class LabController {

    private final LabService labService;

    @Operation(summary = "연구실 썸네일 반환")
    @GetMapping("/thumbnail/{id}")
    public ResponseEntity<LabThumbnailResDTO> getLabThumbnail(@PathVariable("id") String id){
        return ResponseEntity.ok()
                .body(labService.getLabThumbnail(id));
    }

    //잠시 교수님과 연구실을 참조하기 위해 만들어놓음. 추후 삭제 예정
    @GetMapping("/")
    public void joinLab(){
        labService.joinlab();
    }
}
