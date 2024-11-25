package inu.codin.codin.domain.notification.service;

import inu.codin.codin.infra.fcm.FcmConfig;
import inu.codin.codin.infra.redis.RedisStorageService;
import inu.codin.codin.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private NotificationRepository notificationRepository;
    private RedisStorageService redisStorageService;
    private FcmConfig fcmConfig;
}
