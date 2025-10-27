package inu.codin.codin.domain.post.domain.hits.repository;

import inu.codin.codin.domain.post.domain.hits.entity.HitsEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HitsRepository extends MongoRepository<HitsEntity, ObjectId> {

    int countAllByPostId(ObjectId postId);

    boolean existsByPostIdAndUserId(ObjectId postId, ObjectId userId);
}
