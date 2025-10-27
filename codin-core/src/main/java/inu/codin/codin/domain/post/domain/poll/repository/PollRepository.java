package inu.codin.codin.domain.post.domain.poll.repository;

import com.mongodb.client.result.UpdateResult;
import inu.codin.codin.domain.post.domain.poll.entity.PollEntity;
import jakarta.validation.constraints.NotBlank;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PollRepository extends MongoRepository<PollEntity, ObjectId> {
    Optional<PollEntity> findByPostId(ObjectId postId);

    @Query("{ '_id': ?0 }")
    @Update("{ '$inc' : { 'pollVotesCounts.?1' : 1 } }")
    long incOption(ObjectId pollId, int optionIndex);

    @Query("{ '_id': ?0, 'pollVotesCounts.?1': { $gte: 1 } }")
    @Update("{ '$inc' : { 'pollVotesCounts.?1' : -1 } }")
    long dcrOptionIfPositive(ObjectId pollId, int optionIndex);
}
