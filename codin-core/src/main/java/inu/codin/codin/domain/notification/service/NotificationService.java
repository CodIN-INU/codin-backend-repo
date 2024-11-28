package inu.codin.codin.domain.notification.service;

import inu.codin.codin.domain.notification.entity.NotificationEntity;
import inu.codin.codin.domain.notification.repository.NotificationRepository;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.infra.fcm.dto.FcmMessageUserDto;
import inu.codin.codin.infra.fcm.service.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private NotificationRepository notificationRepository;
    private FcmService fcmService;

    /**
     * FCM 메시지를 특정 사용자에게 전송하는 로직
     * @param title 메시지 제목
     * @param body 메시지 내용
     * @param user 메시지를 받을 사용자
     */
    public void sendFcmMessageToUser(String title, String body, UserEntity user) {
        FcmMessageUserDto msgDto = FcmMessageUserDto.builder()
                .user(user)
                .title(title)
                .body(body)
                .build();

        // FCM 메시지 전송
        try {
            fcmService.sendFcmMessage(msgDto);
            log.info("[sendFcmMessage] 알림 전송 성공");
        } catch (Exception e) {
            log.error("[sendFcmMessage] 알림 전송 실패 : {}", e.getMessage());
        }
        // 알림 로그 저장
        saveNotificationLog(msgDto);
    }

    // 알림 로그를 저장하는 로직
    private void saveNotificationLog(FcmMessageUserDto msgDto) {
        NotificationEntity notificationEntity = NotificationEntity.builder()
                .user(msgDto.getUser())
                .type("push")
                .message(msgDto.getBody())
                .priority("high")
                .build();
        notificationRepository.save(notificationEntity);
    }
}
