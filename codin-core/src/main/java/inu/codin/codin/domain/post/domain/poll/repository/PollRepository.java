package inu.codin.codin.domain.post.domain.poll.repository;

import inu.codin.codin.domain.post.domain.poll.entity.PollEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PollRepository extends MongoRepository<PollEntity, ObjectId> {
    Optional<PollEntity> findByPostId(ObjectId postId);

}
