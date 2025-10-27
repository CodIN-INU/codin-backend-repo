package inu.codin.codin.domain.post.domain.best;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface BestRepository extends MongoRepository<BestEntity, ObjectId> {
    Optional<BestEntity> findByPostId(ObjectId postId);
}
