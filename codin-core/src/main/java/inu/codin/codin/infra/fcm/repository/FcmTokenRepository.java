package inu.codin.codin.infra.fcm.repository;

import inu.codin.codin.infra.fcm.entity.FcmTokenEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FcmTokenRepository extends MongoRepository<FcmTokenEntity, String> {

    @Query("{ 'user.id': ?0, deletedAt: null }")
    List<FcmTokenEntity> findAllByUserId(String userId);

    @Query("{ 'fcmToken': ?0, deletedAt: null }")
    Optional<FcmTokenEntity> findByFcmToken(String fcmToken);
}
