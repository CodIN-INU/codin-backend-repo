package inu.codin.codin.domain.post.domain.hits;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface HitsRepository extends MongoRepository<HitsEntity, ObjectId> {

    Optional<HitsEntity> findByPostIdAndUserId(ObjectId postId, ObjectId userId);

    List<HitsEntity> findAllByPostId(ObjectId postId);
}
