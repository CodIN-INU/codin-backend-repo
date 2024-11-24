package inu.codin.codin.domain.like;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends MongoRepository<LikeEntity, String> {
    boolean findByPostIdAndUserId(String postId, String userId);
}
