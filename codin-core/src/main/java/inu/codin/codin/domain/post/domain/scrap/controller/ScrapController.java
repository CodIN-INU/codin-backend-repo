package inu.codin.codin.domain.post.domain.scrap.controller;

import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.post.domain.scrap.service.ScrapService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scraps")
@RequiredArgsConstructor
public class ScrapController {

    private final ScrapService scrapService;

    @Operation(summary = "게시물 스크랩 추가")
    @PostMapping("/{postId}")
    public ResponseEntity<SingleResponse<?>> addScrap(@PathVariable String postId) {
        scrapService.addScrap(postId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SingleResponse<>(201, "스크랩 성공.", null));
    }

    @Operation(summary = "게시물 스크랩 삭제")
    @DeleteMapping("/{postId}")
    public ResponseEntity<SingleResponse<?>> removeScrap(@PathVariable String postId) {
        scrapService.removeScrap(postId);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "스크랩 취소되었습니다.", null));
    }
}

