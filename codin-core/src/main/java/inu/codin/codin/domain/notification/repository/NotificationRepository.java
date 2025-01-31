package inu.codin.codin.domain.notification.repository;

import inu.codin.codin.domain.notification.entity.NotificationEntity;
import inu.codin.codin.domain.user.entity.UserEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<NotificationEntity, ObjectId> {
    @Query("{ 'user': ?0, 'isRead': false, deletedAt: null }")
    long countUnreadNotificationsByUser(UserEntity user);

    List<NotificationEntity> findAllByUser(UserEntity user);
}