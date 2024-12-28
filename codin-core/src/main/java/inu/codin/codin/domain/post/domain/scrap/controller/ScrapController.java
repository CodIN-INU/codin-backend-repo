package inu.codin.codin.domain.post.domain.scrap.controller;

import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.post.domain.like.dto.LikeRequestDto;
import inu.codin.codin.domain.post.domain.scrap.service.ScrapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/scraps")
@RequiredArgsConstructor
@Tag(name = "Scrap API", description = "게시물 스크랩 API")
public class ScrapController {

    private final ScrapService scrapService;

    @Operation(summary = "게시물 스크랩 토글")
    @PostMapping("/{postId}")
    public ResponseEntity<SingleResponse<?>> toggleLike(@PathVariable String postId) {
        String message = scrapService.toggleScrap(postId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SingleResponse<>(201, message, null));
    }

}

