package inu.codin.codin.domain.scrap.repository;

import inu.codin.codin.domain.scrap.entity.ScrapEntity;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScrapRepository extends MongoRepository<ScrapEntity, ObjectId> {

    boolean existsByPostIdAndUserId(ObjectId postId, ObjectId userId);

    int countByPostIdAndDeletedAtIsNull(ObjectId postId);

    Optional<ScrapEntity> findByPostIdAndUserId(ObjectId postId, ObjectId userId);

    Page<ScrapEntity> findAllByUserIdAndDeletedAtIsNullOrderByCreatedAt(ObjectId userId, PageRequest pageRequest);
}
