package inu.codin.codin.domain.post.scrap;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scraps/{postId}")
@RequiredArgsConstructor
public class ScrapController {

    private final ScrapService scrapService;

    @PostMapping("/{userId}")
    public ResponseEntity<String> scrapPost(@PathVariable String postId, @PathVariable String userId) {
        scrapService.addScrap(postId, userId);
        return ResponseEntity.ok("Scrapped successfully");
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> unscrapPost(@PathVariable String postId, @PathVariable String userId) {
        scrapService.removeScrap(postId, userId);
        return ResponseEntity.ok("Unscrapped successfully");
    }

    @GetMapping
    public ResponseEntity<Long> getScrapCount(@PathVariable String postId) {
        long scrapCount = scrapService.getScrapCount(postId);
        return ResponseEntity.ok(scrapCount);
    }
}