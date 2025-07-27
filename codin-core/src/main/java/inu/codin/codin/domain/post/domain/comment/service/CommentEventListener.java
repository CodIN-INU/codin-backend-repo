package inu.codin.codin.domain.post.domain.comment.service;

import inu.codin.codin.domain.notification.service.NotificationService;
import inu.codin.codin.domain.post.domain.comment.dto.event.CommentNotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentEventListener {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handleCommentNotificationEvent(CommentNotificationEvent event){
        notificationService.sendNotificationMessageByComment(event.getPostCategory(), event.getUserId(), event.getPostId(), event.getContent());
    }

}
