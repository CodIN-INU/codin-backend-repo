package inu.codin.codin.domain.user.repository;

import inu.codin.codin.domain.user.entity.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<UserEntity, String> {

    Optional<Object> findByEmail(String email);

    Optional<Object> findByStudentId(String studentId);

}
