package inu.codin.codin.domain.notification.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.domain.notification.entity.NotificationEntity;
import inu.codin.codin.domain.notification.repository.NotificationRepository;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.infra.fcm.dto.FcmMessageTopicDto;
import inu.codin.codin.infra.fcm.dto.FcmMessageUserDto;
import inu.codin.codin.infra.fcm.service.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    private final FcmService fcmService;
    private final String NOTI_COMMENT_TITLE = "누군가가 내 게시글에 댓글을 달았어요!";
    private final String NOTI_REPLY_TITLE = "누군가가 내 댓글에 답글을 달았어요!";


    /**
     * 특정 유저의 읽지 않은 알림 개수를 반환
     * @param user 알림 수신자
     * @return 읽지 않은 알림 개수
     */
    public long getUnreadNotificationCount(UserEntity user) {
        return notificationRepository.countUnreadNotificationsByUser(user);
    }

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
//                .imageUrl() //codin 로고 url
                .build();

        try {
            fcmService.sendFcmMessage(msgDto);
            log.info("[sendFcmMessage] 알림 전송 성공");
            saveNotificationLog(msgDto);
        } catch (Exception e) {
            log.error("[sendFcmMessage] 알림 전송 실패 : {}", e.getMessage());
        }
    }

    /**
     * FCM 메시지를 Topic을 구독한 사람들에게 전송하는 로직
     * @param title
     * @param body
     * @param data
     * @param imageUrl
     * @param topic
     */
    public void sendFcmMessageToTopic(String title, String body, Map<String, String> data, String imageUrl, String topic) {
        FcmMessageTopicDto msgDto = FcmMessageTopicDto.builder()
                .topic(topic)
                .title(title)
                .body(body)
                .data(data)
                .imageUrl(imageUrl)
                .build();

        try {
            fcmService.sendFcmMessageByTopic(msgDto);
            log.info("[sendFcmMessage] 알림 전송 성공");
            saveNotificationLog(msgDto);
        } catch (Exception e) {
            log.error("[sendFcmMessage] 알림 전송 실패 : {}", e.getMessage());
        }
    }

    // 알림 로그를 저장하는 로직 (특정 사용자 대상)
    private void saveNotificationLog(FcmMessageUserDto msgDto) {
        NotificationEntity notificationEntity = NotificationEntity.builder()
                .user(msgDto.getUser())
                .title(msgDto.getTitle())
                .message(msgDto.getBody())
                .type("push")
                .priority("high")
                .build();
        notificationRepository.save(notificationEntity);
    }

    // 알림 로그를 저장하는 로직 (토픽 대상)
    private void saveNotificationLog(FcmMessageTopicDto msgDto) {
        NotificationEntity notificationEntity = NotificationEntity.builder()
                .title(msgDto.getTitle())
                .message(msgDto.getBody())
                .type("topic")
                .priority("high")
                .build();
        notificationRepository.save(notificationEntity);
    }

    public void sendNotificationMessageByComment(ObjectId userId, String content) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));
        sendFcmMessageToUser(NOTI_COMMENT_TITLE, content, user);
    }

    public void sendNotificationMessageByReply(ObjectId userId, String content) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));
        sendFcmMessageToUser(NOTI_REPLY_TITLE, content, user);
    }
}
