package inu.codin.codin.domain.post.domain.scrap;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScrapRepository extends MongoRepository<ScrapEntity, String> {
    boolean findByPostIdAndUserId(String postId, String userId);

    List<ScrapEntity> findByPostId(String postId);

    long countByPostId(String postId);

    void deleteByPostIdAndUserId(String postId, String userId);

    boolean existsByPostIdAndUserId(String postId, String userId);
}
