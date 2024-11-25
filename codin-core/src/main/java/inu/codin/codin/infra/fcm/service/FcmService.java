package inu.codin.codin.infra.fcm.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import inu.codin.codin.domain.notification.entity.NotificationPreference;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.service.UserService;
import inu.codin.codin.infra.fcm.dto.FcmMessageDto;
import inu.codin.codin.infra.fcm.dto.FcmTokenRequest;
import inu.codin.codin.infra.fcm.entity.FcmTokenEntity;
import inu.codin.codin.infra.fcm.repository.FcmTokenRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class FcmService {

    private final UserService userService;
    private final FcmTokenRepository fcmTokenRepository;

    /**
     * 클라이언트로부터 받은 FCM 토큰을 저장하는 로직
     * @param fcmTokenRequest FCM 토큰 요청 DTO
     * @param userDetails 로그인한 유저 정보
     */
    public void saveFcmToken(@Valid FcmTokenRequest fcmTokenRequest, UserDetails userDetails) {
        // 유저 조회
        UserEntity user = null;
        String email = userDetails.getUsername();
        try {
            user = userService.findUserByEmail(email);
        } catch (IllegalArgumentException e) {
            log.error("[saveFcmToken] 유저를 찾을 수 없습니다. email : {}", email);
            return;
        }

        // 이미 존재하는 FCM 토큰이라면 삭제
        fcmTokenRepository.findByFcmToken(fcmTokenRequest.getFcmToken())
                .ifPresent(fcmTokenEntity -> {
                    log.info("[saveFcmToken] 이미 존재하는 FCM 토큰입니다. email : {}, fcmToken : {}", email, fcmTokenRequest.getFcmToken());
                    fcmTokenRepository.delete(fcmTokenEntity);
                });

        FcmTokenEntity fcmTokenEntity = FcmTokenEntity.builder()
                .user(user)
                .fcmToken(fcmTokenRequest.getFcmToken())
                .deviceType(fcmTokenRequest.getDeviceType())
                .build();

        fcmTokenRepository.save(fcmTokenEntity);
    }

    /**
     * FCM 메시지를 전송하는 로직 - 서버 내부 사용
     * @param msgDto FCM 메시지 DTO
     */
    public void sendFcmMessage(FcmMessageDto msgDto) {
        // 유저의 알림 설정 조회
        String userId = msgDto.getUser().getId();
        NotificationPreference userPreference = getUserNotificationPreference(userId);
        // 알림 설정이 Off 라면 전송하지 않음
        if (!userPreference.isAllowPush()) { // todo : NotificationService로 분리
            log.info("[sendFcmMessage] 알림 설정이 Off User : {}", userId);
            return;
        }
        // 유저의 FCM 토큰 조회
        var fcmTokens = getUserFcmToken(userId);
        if (fcmTokens.isEmpty()) {
            log.info("[sendFcmMessage] FCM 토큰이 없습니다. User : {}", userId);
            return;
        }

        // 유저가 가진 모든 토큰으로 FCM 메시지 전송
        for (String token : fcmTokens) {
            try {
                Message message = Message.builder()
                        .setNotification(Notification.builder()
                                .setTitle(msgDto.getTitle())
                                .setBody(msgDto.getBody())
                                .build())
                        .setToken(token)
                        .build();

                String response = FirebaseMessaging.getInstance().send(message);
                log.info("[sendFcmMessage] 알림 전송 성공 : {}", response);
            } catch (Exception e) {
                log.error("[sendFcmMessage] 알림 전송 실패 : {}", e.getMessage());
            }
        }
    }

    // 유저의 알림 설정을 조회하는 로직
    private NotificationPreference getUserNotificationPreference(String userId) {
        return userService.getUserNotificationPreference(userId);
    }

    // 유저의 FCM 토큰을 조회하는 로직
    private List<String> getUserFcmToken(String userId) {
        return fcmTokenRepository.findAllByUserId(userId)
                .stream()
                .map(FcmTokenEntity::getFcmToken)
                .toList();
    }


}
