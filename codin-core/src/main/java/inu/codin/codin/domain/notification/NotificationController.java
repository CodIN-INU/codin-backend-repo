package inu.codin.codin.domain.notification;

import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "알림 읽기")
    @GetMapping("/{notificationId}")
    public ResponseEntity<SingleResponse<?>> readNotification(@PathVariable String notificationId){
        notificationService.readNotification(notificationId);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "알림 읽기 완료", null));
    }

}
