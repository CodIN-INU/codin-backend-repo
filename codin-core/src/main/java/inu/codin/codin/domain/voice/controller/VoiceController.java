package inu.codin.codin.domain.voice.controller;

import inu.codin.codin.common.dto.Department;
import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.voice.dto.VoiceBoxAnswerRequest;
import inu.codin.codin.domain.voice.dto.VoiceBoxCreateRequest;
import inu.codin.codin.domain.voice.dto.VoiceBoxDetailResponse;
import inu.codin.codin.domain.voice.dto.VoiceBoxPageResponse;
import inu.codin.codin.domain.voice.service.VoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/voice-box")
@Tag(name = "Voice Box API", description = "[리다지인] 익명의 소리함 API")
@RequiredArgsConstructor
public class VoiceController {

    private final VoiceService voiceService;

    // 1. 소리함 생성 메서드
    @Operation(summary = "익명 질문 생성")
    @PostMapping
    public ResponseEntity<SingleResponse<VoiceBoxDetailResponse>> createVoiceBox(
            @RequestBody @Valid VoiceBoxCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new SingleResponse<>(201, "익명 목소리 생성 완료",
                voiceService.createVoiceBox(request)));
    }

    // 2. 익명 소리함 반환 리스트
    @Operation(summary = "답변된 소리함 리스트 반환")
    @GetMapping
    public ResponseEntity<SingleResponse<VoiceBoxPageResponse>> getVoiceBoxListByDepartment(
            @RequestParam Department department,
            @RequestParam("page") @NotNull int pageNumber
    ) {
        return ResponseEntity.ok().body(new SingleResponse<>(200, "답변된 소리함 리스트 반환 성공",
                voiceService.getVoiceBoxListByDepartment(department, pageNumber)));
    }

    // 3. 소리함 내용 공감 메서드
    @Operation(summary = "소리함 내용 공감 토글(On/Off)")
    @PostMapping("/vote/{boxId}")
    public ResponseEntity<SingleResponse<?>> toggleVoiceBox(
            @PathVariable("boxId") String boxId,
            @RequestParam @NotNull Boolean positive
    ) {
        voiceService.toggleVoiceBox(boxId, positive);
        return ResponseEntity.ok().body(new SingleResponse<>(200, "소리함 내용 공감 토글(On/Off) 성공", null));
    }

    // 4. 답변되지 않은 소리함 내용 반환
    @Operation(summary = "[학생회] 답변되지 않은 소리함 리스트 반환")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @GetMapping("/not-answered")
    public ResponseEntity<SingleResponse<VoiceBoxPageResponse>> getAllNotAnsweredList(
            @RequestParam Department department,
            @RequestParam("page") @NotNull int pageNumber
    ) {
        return ResponseEntity.ok().body(new SingleResponse<>(200, "[학생회] 답변되지 않은 소리함 리스트 반환 성공",
                voiceService.getAllNotAnsweredList(department, pageNumber)));
    }

    // 5. 소리함 내용 답변 추가
    @Operation(summary = "[학생회] 답변되지 않은 소리함 내용에 답변 추가")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PostMapping("/not-answered/{boxId}")
    public ResponseEntity<SingleResponse<VoiceBoxDetailResponse>> addAnswer(
            @PathVariable("boxId") String boxId,
            @RequestBody @Valid VoiceBoxAnswerRequest request
            ) {
        return ResponseEntity.ok().body(new SingleResponse<>(200, "[학생회] 답변되지 않은 소리함 내용에 답변 추가",
                voiceService.addAnswer(boxId, request.getAnswer())));
    }

    // 6. 질문 삭제
    @Operation(summary = "[관리자] 소리함 질문 삭제")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @DeleteMapping("/{boxId}")
    public ResponseEntity<SingleResponse<?>> deleteVoiceBox(
            @PathVariable String boxId
    ) {
        voiceService.deleteVoiceBox(boxId);
        return ResponseEntity.ok().body(new SingleResponse<>(200, "[관리자] 소리함 질문 삭제", null));
    }

}
