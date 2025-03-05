package inu.codin.codin.domain.user.repository;

import inu.codin.codin.domain.user.entity.UserEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<UserEntity, ObjectId> {
    @Query("{'_id':  ?0, 'deletedAt': null}")
    Optional<UserEntity> findByUserId(ObjectId userId);

    @Query("{'email':  ?0, 'deletedAt': null, 'status':  { $in:  ['ACTIVE'] }}")
    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByNicknameAndDeletedAtIsNull(String nickname);

    @Query("{'email':  ?0, 'deletedAt': null, 'status':  { $in:  ['DISABLED'] }}")
    Optional<UserEntity> findByEmailAndDisabled(String email);

    @Query("{'email':  ?0, 'deletedAt': null }")
    Optional<UserEntity> findByEmailAndStatusAll(String email);
}
