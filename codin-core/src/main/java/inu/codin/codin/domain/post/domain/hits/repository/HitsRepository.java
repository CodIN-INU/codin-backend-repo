package inu.codin.codin.domain.post.domain.hits.repository;

import inu.codin.codin.domain.post.domain.hits.entity.HitsEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface HitsRepository extends MongoRepository<HitsEntity, ObjectId> {

    int countAllByPostId(ObjectId postId);

    boolean existsByPostIdAndUserId(ObjectId postId, ObjectId userId);

    List<HitsEntity> findAllByPostId(ObjectId postId);
}
