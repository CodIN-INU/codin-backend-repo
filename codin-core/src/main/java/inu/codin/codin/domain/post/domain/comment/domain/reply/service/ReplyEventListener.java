package inu.codin.codin.domain.post.domain.comment.domain.reply.service;

import inu.codin.codin.domain.notification.service.NotificationService;
import inu.codin.codin.domain.post.domain.comment.domain.reply.dto.event.ReplyNotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReplyEventListener {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handleReplyNotificationEvent(ReplyNotificationEvent event){
        notificationService.sendNotificationMessageByReply(event.getPostCategory(), event.getUserId(), event.getPostId(), event.getContent());
    }

}