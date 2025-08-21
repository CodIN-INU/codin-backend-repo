package inu.codin.codin.domain.notification.repository;

import inu.codin.codin.domain.notification.entity.NotificationEntity;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<NotificationEntity, ObjectId> {
    @Query("{ 'userId': ?0, 'isRead': false, deletedAt: null }")
    long countUnreadNotificationsByUserId(ObjectId userId);

    @Query("{ 'userId': ?0, 'isRead': false, deletedAt: null }")
    List<NotificationEntity> findAllByUserId(ObjectId userId, Sort sort);
}