package inu.codin.codin.domain.block.controller;

import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.block.service.BlockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/block")
@RequiredArgsConstructor
@Tag(name = "Block API", description = "사용자 차단 기능")
public class BlockController {
    private final BlockService blockService;

    @Operation(
            summary = "사용자 차단하기"
    )
    @PostMapping("/{blockedUserId}")
    public ResponseEntity<?> blockUser(@PathVariable String blockedUserId) {
        blockService.blockUser(blockedUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SingleResponse<>(201, "사용자 차단 완료", null));
    }

    @Operation(
            summary = "사용자 차단 해제"
    )
    @DeleteMapping("/{blockedUserId}")
    public ResponseEntity<?> unblockUser(@PathVariable String blockedUserId) {
        blockService.unblockUser(blockedUserId);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "사용자 차단 해제 완료", null));
    }
}
