package inu.codin.codin.domain.post.domain.best;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BestRepository extends MongoRepository<BestEntity, ObjectId> {
    boolean existsByPostId(ObjectId postId);
}
