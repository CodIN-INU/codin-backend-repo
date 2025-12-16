package inu.codin.codin.domain.notification.controller;

import inu.codin.codin.common.response.ListResponse;
import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.notification.dto.request.OneCharNameRequestDto;
import inu.codin.codin.domain.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
@Tag(name = "Notification API", description = "알림 API")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(
            summary = "알림 내역 반환"
    )
    @GetMapping()
    public ResponseEntity<ListResponse<?>> getNotifications(){
        return ResponseEntity.ok()
                .body(new ListResponse<>(200, "알림 내역 반환 완료",
                        notificationService.getNotification()));
    }

    @Operation(summary = "알림 읽기")
    @GetMapping("/{notificationId}")
    public ResponseEntity<SingleResponse<?>> readNotification(@PathVariable String notificationId){
        notificationService.readNotification(notificationId);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "알림 읽기 완료", null));
    }

    @Operation(summary = "이름 1글자 대상 1회성 알림 발송 (관리자)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/onceCharName")
    public ResponseEntity<SingleResponse<?>> sendNameFix(
            @RequestBody @Valid OneCharNameRequestDto request) {

        int sent = notificationService.sendOneCharNameFix(request);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "1회성 알림 발송 완료", Map.of("sent", sent)));
    }

    @Operation(summary = "이름 1글자 대상 1회성 알림 발송 테스트 (관리자)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/test")
    public ResponseEntity<SingleResponse<?>> sendNameFixTest(
            @RequestBody @Valid OneCharNameRequestDto request) {

        int sent = notificationService.sendOneCharNameFixTest(request);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "1회성 알림 발송 완료", Map.of("sent", sent)));
    }

}
