package inu.codin.codin.infra.fcm.repository;

import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.infra.fcm.entity.FcmTokenEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FcmTokenRepository extends MongoRepository<FcmTokenEntity, ObjectId> {
    @Query("{ 'user': ?0, deletedAt: null }")
    Optional<FcmTokenEntity> findByUser(UserEntity user);
}
