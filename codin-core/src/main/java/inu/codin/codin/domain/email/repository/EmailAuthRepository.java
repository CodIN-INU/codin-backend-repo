package inu.codin.codin.domain.email.repository;

import inu.codin.codin.domain.email.entity.EmailAuthEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailAuthRepository extends MongoRepository<EmailAuthEntity, String> {

    Boolean existsByEmail(String email);

    Optional<EmailAuthEntity> findByEmailAndAuthNum(String email, String authNum);
}
