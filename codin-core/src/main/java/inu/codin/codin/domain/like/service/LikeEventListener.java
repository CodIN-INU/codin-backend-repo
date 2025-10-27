package inu.codin.codin.domain.like.service;

import inu.codin.codin.domain.like.dto.event.LikeNotificationEvent;
import inu.codin.codin.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class LikeEventListener {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handleLikeNotificationEvent(LikeNotificationEvent event){
        notificationService.sendNotificationMessageByLike(event.getLikeType(), event.getLikeTypeId());
    }
}
