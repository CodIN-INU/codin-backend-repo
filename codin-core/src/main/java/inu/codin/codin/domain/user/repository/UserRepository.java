package inu.codin.codin.domain.user.repository;

import inu.codin.codin.domain.user.entity.UserEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<UserEntity, ObjectId> {

    @Query("{'email':  ?0, 'deletedAt': null, 'status':  { $in:  ['ACTIVE'] }}")
    Optional<UserEntity> findByEmail(String email);

    @Query("{'studentId':  ?0, 'deletedAt': null, 'status':  { $in:  ['ACTIVE'] }}")
    Optional<UserEntity> findByStudentId(String studentId);

    Optional<UserEntity> findById(String id);

}
