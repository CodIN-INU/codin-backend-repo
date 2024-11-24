package inu.codin.codin.domain.scrap;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScrapRepository extends MongoRepository<ScrapEntity, String> {
    boolean findByPostIdAndUserId(String postId, String userId);
}
