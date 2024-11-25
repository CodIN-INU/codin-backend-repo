package inu.codin.codin.domain.notification.repository;

import inu.codin.codin.domain.notification.entity.NotificationEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends MongoRepository<NotificationEntity, String> {
}
