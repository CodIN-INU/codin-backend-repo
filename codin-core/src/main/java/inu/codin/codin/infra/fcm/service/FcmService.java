package inu.codin.codin.infra.fcm.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import inu.codin.codin.domain.notification.entity.NotificationPreference;
import inu.codin.codin.infra.fcm.dto.FcmMessageDto;
import inu.codin.codin.infra.fcm.dto.FcmTokenRequest;
import inu.codin.codin.infra.fcm.entity.FcmTokenEntity;
import inu.codin.codin.infra.fcm.exception.FcmTokenNotFoundException;
import inu.codin.codin.infra.fcm.repository.FcmTokenRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FcmService {

    private final FcmTokenRepository fcmTokenRepository;

    /**
     * 클라이언트로부터 받은 FCM 토큰을 저장하는 로직
     * @param fcmTokenRequest FCM 토큰 요청 DTO
     * @param userDetails 로그인한 유저 정보
     */
    public void saveFcmToken(@Valid FcmTokenRequest fcmTokenRequest, UserDetails userDetails) {
        // 유저의 FCM 토큰이 존재하는지 확인
        String email = userDetails.getUsername();
        Optional<FcmTokenEntity> fcmToken = fcmTokenRepository.findByEmail(email);

        if (fcmToken.isPresent()) { // 이미 존재하는 유저라면 토큰 추가
            FcmTokenEntity fcmTokenEntity = fcmToken.get();
            fcmTokenEntity.addFcmToken(fcmTokenRequest.getFcmToken());
            fcmTokenRepository.save(fcmTokenEntity);
        }
        else { // 존재하지 않는 FCM 토큰이라면 저장
            FcmTokenEntity newFcmTokenEntity = FcmTokenEntity.builder()
                    .email(email)
                    .fcmTokenList(List.of(fcmTokenRequest.getFcmToken()))
                    .deviceType(fcmTokenRequest.getDeviceType())
                    .build();
            fcmTokenRepository.save(newFcmTokenEntity);
        }
    }

    /**
     * FCM 메시지를 전송하는 로직 - 서버 내부 사용
     * @param msgDto FCM 메시지 DTO
     */
    public void sendFcmMessage(FcmMessageDto msgDto) {
        // 유저의 알림 설정 조회
        String email = msgDto.getUser().getEmail();
        NotificationPreference userPreference = msgDto.getUser().getNotificationPreference();

        // 유저의 FCM 토큰 조회
        FcmTokenEntity fcmTokenEntity = fcmTokenRepository.findByEmail(email).orElseThrow(()
                -> new FcmTokenNotFoundException("유저에게 FCM 토큰이 존재하지 않습니다."));

        // 알림 설정에 따라 알림 전송
        if (!userPreference.isAllowPush()) {
            log.info("[sendFcmMessage] 알림 설정에서 푸시 알림을 허용하지 않았습니다. : {}", email);
            return;
        }
        for (String fcmToken : fcmTokenEntity.getFcmTokenList()) {
            try {
                Message message = Message.builder()
                        .setNotification(Notification.builder()
                                .setTitle(msgDto.getTitle())
                                .setBody(msgDto.getBody())
                                .setImage(msgDto.getImageUrl())
                                .build())
                        .setToken(fcmToken)
                        .putAllData(msgDto.getData())
                        .build();

                String response = FirebaseMessaging.getInstance().send(message);
                log.info("[sendFcmMessage] 알림 전송 성공 : {}", response);
            } catch (FirebaseMessagingException e) {
                log.error("[sendFcmMessage] 알림 전송 실패, errorCode : {}, msg : {}", e.getErrorCode(), e.getMessage());
                // todo : 에러 관리 및 리포팅 기능 추가
                // todo : 알림 전송 실패 시 로직 추가 FCM 토큰 만료시 삭제 로직 추가
                // todo : 토큰 만료 관리 추가
            }
        }
    }

    // todo : FCM Bulk 메시지 전송 로직 추가
    // todo : FCM 토칙 기반 메세지 전송 로직 추가 - 공지사항, 학과별 알림, 게시글 내 모든 댓글 인원에게 알림

}