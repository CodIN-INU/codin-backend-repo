package inu.codin.codin.domain.notification.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.like.entity.LikeType;
import inu.codin.codin.domain.notification.dto.response.NotificationListResponseDto;
import inu.codin.codin.domain.notification.entity.NotificationEntity;
import inu.codin.codin.domain.notification.repository.NotificationRepository;
import inu.codin.codin.domain.post.domain.comment.entity.CommentEntity;
import inu.codin.codin.domain.post.domain.comment.repository.CommentRepository;
import inu.codin.codin.domain.post.domain.comment.domain.reply.entity.ReplyCommentEntity;
import inu.codin.codin.domain.post.domain.comment.domain.reply.repository.ReplyCommentRepository;
import inu.codin.codin.domain.post.entity.PostCategory;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.infra.fcm.dto.FcmMessageTopicDto;
import inu.codin.codin.infra.fcm.dto.FcmMessageUserDto;
import inu.codin.codin.infra.fcm.service.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReplyCommentRepository replyCommentRepository;

    private final FcmService fcmService;
    private final String NOTI_COMMENT = "댓글이 달렸습니다: ";
    private final String NOTI_REPLY = "대댓글이 달렸습니다: ";
    private final String NOTI_LIKE = "좋아요가 달렸습니다";
    private final String NOTI_CHAT = "새로운 채팅이 있습니다.";


    /**
     * 특정 유저의 읽지 않은 알림 개수를 반환
     * @param userId 알림 수신자의 _id
     * @return 읽지 않은 알림 개수
     */
    public long getUnreadNotificationCount(ObjectId userId) {
        return notificationRepository.countUnreadNotificationsByUserId(userId);
    }

    /**
     * FCM 메시지를 특정 사용자에게 전송하는 로직
     * @param title 메시지 제목
     * @param body 메시지 내용
     * @param data 알림 대상의 _id
     * @param userId 메시지를 받을 사용자의 _id
     */
    public void sendFcmMessageToUser(String title, String body, Map<String, String> data, ObjectId userId) {
        FcmMessageUserDto msgDto = FcmMessageUserDto.builder()
                .userId(userId)
                .title(title)
                .body(body)
                .data(data)
//                .imageUrl() //codin 로고 url
                .build();

        try {
            fcmService.sendFcmMessage(msgDto);
            log.info("[sendFcmMessage] 알림 전송 성공");
        } catch (Exception e) {
            log.error("[sendFcmMessage] 알림 전송 실패 : {}", e.getMessage());
        }
        saveNotificationLog(msgDto, data);
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
    private void saveNotificationLog(FcmMessageUserDto msgDto, Map<String, String> data) {
        NotificationEntity notificationEntity = NotificationEntity.builder()
                .userId(msgDto.getUserId())
                .title(msgDto.getTitle())
                .message(msgDto.getBody())
                .targetId(new ObjectId(data.get("id")))
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

    public void sendNotificationMessageByComment(PostCategory postCategory, ObjectId userId, String postId, String content) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));
        Map<String, String> post = new HashMap<>();
        post.put("id", postId);
        String title = postCategory.getDescription().split("_")[0];
        sendFcmMessageToUser(title, NOTI_COMMENT+content, post, userId);
    }

    public void sendNotificationMessageByReply(PostCategory postCategory, ObjectId userId, String postId, String content) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));
        Map<String, String> post = new HashMap<>();
        post.put("id", postId);
        String title = postCategory.getDescription().split("_")[0];
        sendFcmMessageToUser(title, NOTI_REPLY+content, post, userId);
    }

    public void sendNotificationMessageByLike(LikeType likeType, ObjectId likeTypeId) {
        switch(likeType){
            case POST -> {
                PostEntity postEntity = postRepository.findByIdAndNotDeleted(likeTypeId)
                        .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다."));
                userRepository.findById(postEntity.getUserId())
                        .orElseThrow(() -> new NotFoundException("유저 정보를 찾을 수 없습니다."));
                Map<String, String> post = new HashMap<>();
                post.put("id", postEntity.get_id().toString());
                sendFcmMessageToUser(NOTI_LIKE, "내 게시글 보러 가기", post, postEntity.getUserId());
            }
            case REPLY -> {
                ReplyCommentEntity replyCommentEntity = replyCommentRepository.findByIdAndNotDeleted(likeTypeId)
                        .orElseThrow(() -> new NotFoundException("대댓글을 찾을 수 없습니다."));
                CommentEntity commentEntity = commentRepository.findByIdAndNotDeleted(replyCommentEntity.getCommentId())
                        .orElseThrow(() -> new NotFoundException("댓글을 찾을 수 없습니다."));
                PostEntity postEntity = postRepository.findByIdAndNotDeleted(commentEntity.getPostId())
                        .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다."));
                userRepository.findById(replyCommentEntity.getUserId())
                        .orElseThrow(() -> new NotFoundException("유저 정보를 찾을 수 없습니다."));
                Map<String, String> post = new HashMap<>();
                post.put("id", postEntity.get_id().toString());
                sendFcmMessageToUser(NOTI_LIKE, "내 답글 보러 가기", post, replyCommentEntity.getUserId());
            }
            case COMMENT -> {
                CommentEntity commentEntity = commentRepository.findByIdAndNotDeleted(likeTypeId)
                        .orElseThrow(() -> new NotFoundException("댓글을 찾을 수 없습니다."));
                PostEntity postEntity = postRepository.findByIdAndNotDeleted(commentEntity.getPostId())
                        .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다."));
                userRepository.findById(commentEntity.getUserId())
                        .orElseThrow(() -> new NotFoundException("유저 정보를 찾을 수 없습니다."));
                Map<String, String> post = new HashMap<>();
                post.put("id", postEntity.get_id().toString());
                sendFcmMessageToUser(NOTI_LIKE, "내 댓글 보러 가기", post, commentEntity.getUserId());
            }
        }
    }

    public void sendNotificationMessageByChat(ObjectId userId, ObjectId chatRoomId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("유저 정보를 찾을 수 없습니다."));
        Map<String, String> chat = new HashMap<>();
        chat.put("id", chatRoomId.toString());
        sendFcmMessageToUser("익명 채팅방", NOTI_CHAT, chat, userId);
    }

    public void readNotification(String notificationId){
        NotificationEntity notificationEntity = notificationRepository.findById(new ObjectId(notificationId))
                .orElseThrow(() -> new NotFoundException("해당 알림을 찾을 수 없습니다."));
        notificationEntity.markAsRead();
        notificationRepository.save(notificationEntity);
    }

    public List<NotificationListResponseDto> getNotification() {
        //todo 유저에게 맞는 토픽 알림들도 반환
        ObjectId userId = SecurityUtils.getCurrentUserId();
        userRepository.findById(userId)
                        .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다"));

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        List<NotificationEntity> notifications = notificationRepository.findAllByUserId(userId, sort);
        return notifications.stream()
                .map(NotificationListResponseDto::of)
                .toList();
    }
}
