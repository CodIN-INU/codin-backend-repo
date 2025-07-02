package inu.codin.codin.domain.post.domain.poll.repository;

import inu.codin.codin.domain.post.domain.poll.entity.PollVoteEntity;
import jakarta.validation.constraints.NotBlank;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PollVoteRepository extends MongoRepository<PollVoteEntity, ObjectId> {
    boolean existsByPollIdAndUserId(ObjectId pollId, ObjectId userId);

    long countByPollId(@NotBlank ObjectId id);

    Optional<PollVoteEntity> findByPollIdAndUserId(@NotBlank ObjectId id, ObjectId userId);
}
