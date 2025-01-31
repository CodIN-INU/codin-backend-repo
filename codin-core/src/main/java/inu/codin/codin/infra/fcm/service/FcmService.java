package inu.codin.codin.infra.fcm.service;

import com.google.firebase.messaging.*;
import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.notification.entity.NotificationPreference;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.domain.user.service.UserService;
import inu.codin.codin.infra.fcm.dto.FcmMessageTopicDto;
import inu.codin.codin.infra.fcm.dto.FcmMessageUserDto;
import inu.codin.codin.infra.fcm.dto.FcmTokenRequest;
import inu.codin.codin.infra.fcm.entity.FcmTokenEntity;
import inu.codin.codin.infra.fcm.exception.FcmTokenNotFoundException;
import inu.codin.codin.infra.fcm.repository.FcmTokenRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FcmService {

    private final FcmTokenRepository fcmTokenRepository;
    private final UserRepository userRepository;

    /**
     * 클라이언트로부터 받은 FCM 토큰을 저장하는 로직
     * @param fcmTokenRequest FCM 토큰 요청 DTO
     */
    public void saveFcmToken(@Valid FcmTokenRequest fcmTokenRequest) {
        // 유저의 FCM 토큰이 존재하는지 확인
        ObjectId userId = SecurityUtils.getCurrentUserId();
        UserEntity user = getUserEntityFromUserId(userId);
        Optional<FcmTokenEntity> fcmToken = fcmTokenRepository.findByUser(user);

        if (fcmToken.isPresent()) { // 이미 존재하는 유저라면 토큰 추가
            FcmTokenEntity fcmTokenEntity = fcmToken.get();
            fcmTokenEntity.addFcmToken(fcmTokenRequest.getFcmToken());
            fcmTokenRepository.save(fcmTokenEntity);
        }
        else { // 존재하지 않는 FCM 토큰이라면 저장
            FcmTokenEntity newFcmTokenEntity = FcmTokenEntity.builder()
                    .user(user)
                    .fcmTokenList(List.of(fcmTokenRequest.getFcmToken()))
                    .deviceType(fcmTokenRequest.getDeviceType())
                    .build();
            fcmTokenRepository.save(newFcmTokenEntity);
        }
    }

    /**
     * FCM 메시지를 전송하는 로직 - 서버 내부 사용
     * @param fcmMessageUserDto FCM 메시지 유저 DTO
     */
    public void sendFcmMessage(FcmMessageUserDto fcmMessageUserDto) {
        // 유저의 알림 설정 조회
        UserEntity user = fcmMessageUserDto.getUser();
        NotificationPreference userPreference = fcmMessageUserDto.getUser().getNotificationPreference();

        // 유저의 FCM 토큰 조회
        FcmTokenEntity fcmTokenEntity = fcmTokenRepository.findByUser(user).orElseThrow(()
                -> new FcmTokenNotFoundException("유저에게 FCM 토큰이 존재하지 않습니다."));

        // 알림 설정에 따라 알림 전송
        if (!userPreference.isAllowPush()) {
            log.info("[sendFcmMessage] 알림 설정에서 푸시 알림을 허용하지 않았습니다. : {}", user.getEmail());
            return;
        }
        for (String fcmToken : fcmTokenEntity.getFcmTokenList()) {
            try {
                Message message = Message.builder()
                        .setNotification(Notification.builder()
                                .setTitle(fcmMessageUserDto.getTitle())
                                .setBody(fcmMessageUserDto.getBody())
                                .setImage(fcmMessageUserDto.getImageUrl())
                                .build())
                        .setToken(fcmToken)
                        .putAllData(fcmMessageUserDto.getData())
                        .build();

                String response = FirebaseMessaging.getInstance().send(message);
                log.info("[sendFcmMessage] 알림 전송 성공 : {}", response);
            } catch (FirebaseMessagingException e) {
                log.error("[sendFcmMessage] 알림 전송 실패, errorCode : {}, msg : {}", e.getErrorCode(), e.getMessage());
                handleFirebaseMessagingException(e, fcmTokenEntity, fcmToken);
            }
        }
    }

    // todo : FCM Bulk 메시지 전송 로직 추가
    // todo : FCM 토칙 기반 메세지 전송 로직 추가 - 공지사항, 학과별 알림, 게시글 내 모든 댓글 인원에게 알림

    /**
     * FCM 메시지를 특정 토픽으로 전송하는 로직 - 서버 내부 로직
     * 브로드 캐스팅 알림을 위해 사용
     * @param fcmMessageTopicDto FCM 메시지 토픽 DTO
     */
    public void sendFcmMessageByTopic(FcmMessageTopicDto fcmMessageTopicDto) {
        //
        try {
            Message message = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(fcmMessageTopicDto.getTitle())
                            .setBody(fcmMessageTopicDto.getBody())
                            .setImage(fcmMessageTopicDto.getImageUrl())
                            .build())
                    .setTopic(fcmMessageTopicDto.getTopic())
                    .putAllData(fcmMessageTopicDto.getData())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("[sendFcmMessageByTopic] 알림 전송 성공 : {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("[sendFcmMessageByTopic] 알림 전송 실패, errorCode : {}, msg : {}", e.getErrorCode(), e.getMessage());
        }
    }

    /**
     * FCM 메시지 전송 중 발생한 예외를 처리하는 로직
     * @param e FirebaseMessagingException
     * @param fcmTokenEntity FcmTokenEntity
     * @param fcmToken FCM 토큰
     */
    private void handleFirebaseMessagingException(FirebaseMessagingException e, FcmTokenEntity fcmTokenEntity, String fcmToken) {
        MessagingErrorCode errorCode = e.getMessagingErrorCode();
        switch (errorCode) {
            case INVALID_ARGUMENT: // FCM 토큰이 유효하지 않을 때
                log.error("Invalid argument error for token: {}", fcmToken);
                break;
            case UNREGISTERED: // FCM 토큰이 등록되지 않았을 때
                log.warn("Unregistered token: {}. Removing from database.", fcmToken);
                removeFcmToken(fcmTokenEntity, fcmToken);
                break;
            case QUOTA_EXCEEDED: // FCM 토큰의 전송량이 초과되었을 때
                log.error("Quota exceeded for token: {}", fcmToken);
                // 에러관리 및 리포팅 기능 추가
                break;
            case SENDER_ID_MISMATCH: // FCM 토큰의 발신자 ID가 일치하지 않을 때
                log.error("Sender ID mismatch for token: {}", fcmToken);
                break;
            case THIRD_PARTY_AUTH_ERROR: // FCM 토큰의 인증이 실패했을 때
                log.error("Third-party authentication error for token: {}", fcmToken);
                break;
            default: // 그 외의 에러
                log.error("Unknown error for token: {}", fcmToken);
                break;
        }
    }

    // todo : FCM 토큰 만료시 삭제 로직 추가
    private void removeFcmToken(FcmTokenEntity fcmTokenEntity, String fcmToken) {
        fcmTokenEntity.deleteFcmToken(fcmToken);
        fcmTokenRepository.save(fcmTokenEntity);
    }

    /**
     * FCM 토픽 구독 로직
     * @param topic 구독할 토픽 이름
     */
    public void subscribeTopic(String topic) {
        ObjectId userId = SecurityUtils.getCurrentUserId();
        UserEntity user = getUserEntityFromUserId(userId);
        FcmTokenEntity fcmTokenEntity = fcmTokenRepository.findByUser(user)
                .orElseThrow(() -> new FcmTokenNotFoundException("유저의 FCM 토큰이 존재하지 않습니다."));

        for (String token : fcmTokenEntity.getFcmTokenList()) {
            try {
                FirebaseMessaging.getInstance().subscribeToTopic(List.of(token), topic);
                log.info("FCM 토픽 구독 성공: 토픽={}, 토큰={}", topic, token);
            } catch (FirebaseMessagingException e) {
                log.error("FCM 토픽 구독 실패: 토픽={}, 토큰={}, 에러={}", topic, token, e.getMessage());
            }
        }
    }

    /**
     * FCM 토픽 구독 해제 로직
     * @param topic 구독 해제할 토픽 이름
     */
    public void unsubscribeTopic(String topic) {
        ObjectId userId = SecurityUtils.getCurrentUserId();
        UserEntity user = getUserEntityFromUserId(userId);
        FcmTokenEntity fcmTokenEntity = fcmTokenRepository.findByUser(user)
                .orElseThrow(() -> new FcmTokenNotFoundException("유저의 FCM 토큰이 존재하지 않습니다."));

        for (String token : fcmTokenEntity.getFcmTokenList()) {
            try {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(List.of(token), topic);
                log.info("FCM 토픽 구독 해제 성공: 토픽={}, 토큰={}", topic, token);
            } catch (FirebaseMessagingException e) {
                log.error("FCM 토픽 구독 해제 실패: 토픽={}, 토큰={}, 에러={}", topic, token, e.getMessage());
            }
        }
    }

    public UserEntity getUserEntityFromUserId(ObjectId userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 이메일에 대한 유저 정보를 찾을 수 없습니다."));
    }

}